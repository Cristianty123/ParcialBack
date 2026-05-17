package com.labback.service;

import com.labback.dto.ChatMessageResponse;
import com.labback.dto.ConversationItemResponse;
import com.labback.dto.PagedResponse;
import com.labback.dto.SendMessageRequest;
import com.labback.model.ChatMessage;
import com.labback.model.User;
import com.labback.repository.ChatMessageRepository;
import com.labback.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository        userRepository;

    public ChatService(ChatMessageRepository chatMessageRepository,
                       UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository        = userRepository;
    }

    // ---------------------------------------------------------------
    // HU-16: GET /chat/conversations
    // ---------------------------------------------------------------

    /**
     * Devuelve la bandeja de conversaciones del usuario autenticado,
     * ordenada por el mensaje más reciente de cada conversación.
     *
     * Estrategia en dos pasos para evitar que JPQL/Hibernate haga N+1:
     *   1. Query nativa que devuelve el ID del último mensaje por canal.
     *   2. Carga esos mensajes con JOIN FETCH de sender y receiver.
     *   3. Construye cada ConversationItemResponse determinando quién
     *      es el "partner" (el que no soy yo) y consultando unreadCount.
     */
    @Transactional(readOnly = true)
    public List<ConversationItemResponse> getConversations(String username) {
        User me = findByUsernameOrThrow(username);

        List<Integer> lastMessageIds =
                chatMessageRepository.findLastMessageIdPerConversation(me.getId());

        if (lastMessageIds.isEmpty()) {
            return List.of();
        }

        List<ChatMessage> lastMessages =
                chatMessageRepository.findByIdsWithUsers(lastMessageIds);

        return lastMessages.stream()
                .map(msg -> {
                    // El partner es quien NO soy yo en este mensaje
                    User partner = msg.getSender().getId().equals(me.getId())
                            ? msg.getReceiver()
                            : msg.getSender();

                    // Mensajes no leídos que partner me envió a mí
                    Long unread = chatMessageRepository.countUnread(me.getId(), partner.getId());

                    return ConversationItemResponse.builder()
                            .partnerId(partner.getId())
                            .partnerName(partner.getFullName() != null
                                    ? partner.getFullName()
                                    : partner.getUsername())
                            .partnerPhoto(partner.getPhotoUrl())
                            .lastMessage(msg.getContent())
                            .lastMessageAt(msg.getSentAt())
                            .unreadCount(unread)
                            .build();
                })
                // Ordenar por lastMessageAt DESC (el más reciente primero)
                .sorted((a, b) -> b.getLastMessageAt().compareTo(a.getLastMessageAt()))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // HU-17: POST /chat/messages — enviar mensaje
    // ---------------------------------------------------------------

    /**
     * Persiste un nuevo mensaje del usuario autenticado hacia receiverId.
     * isRead = false por defecto (el modelo ya lo establece).
     */
    @Transactional
    public ChatMessageResponse sendMessage(String username, SendMessageRequest req) {
        User sender   = findByUsernameOrThrow(username);
        User receiver = userRepository.findById(req.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Destinatario no encontrado con id: " + req.getReceiverId()));

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("No puedes enviarte mensajes a ti mismo");
        }

        ChatMessage message = ChatMessage.builder()
                .content(req.getContent())
                .sender(sender)
                .receiver(receiver)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        return toChatMessageResponse(saved, sender.getId());
    }

    // ---------------------------------------------------------------
    // HU-17: GET /chat/messages/{partnerId} — historial paginado
    // ---------------------------------------------------------------

    /**
     * Devuelve el historial paginado entre el usuario autenticado y partnerId.
     * HU-17 CA-4: solo lectura si eres sender o receiver (verificado implícitamente
     * porque la query filtra por (userId, partnerId) — si no hay mensajes entre
     * ellos, simplemente devuelve vacío sin exponer datos de otros).
     */
    @Transactional(readOnly = true)
    public PagedResponse<ChatMessageResponse> getHistory(
            String username, Integer partnerId, int page, int size) {

        User me = findByUsernameOrThrow(username);

        // Verificar que el partner existe
        if (!userRepository.existsById(partnerId)) {
            throw new IllegalArgumentException("Usuario no encontrado con id: " + partnerId);
        }

        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "sentAt"));

        Page<ChatMessage> resultPage = chatMessageRepository
                .findConversationHistory(me.getId(), partnerId, pageable);

        List<ChatMessageResponse> content = resultPage.getContent().stream()
                .map(msg -> toChatMessageResponse(msg, me.getId()))
                .collect(Collectors.toList());

        return PagedResponse.<ChatMessageResponse>builder()
                .content(content)
                .totalElements(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .currentPage(resultPage.getNumber())
                .build();
    }

    // ---------------------------------------------------------------
    // HU-17: PATCH /chat/messages/read/{partnerId} — marcar como leídos
    // ---------------------------------------------------------------

    /**
     * Marca como leídos todos los mensajes que partnerId envió al usuario autenticado.
     * Devuelve el número de mensajes actualizados.
     */
    @Transactional
    public int markAsRead(String username, Integer partnerId) {
        User me = findByUsernameOrThrow(username);
        return chatMessageRepository.markAsRead(partnerId, me.getId());
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private User findByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + username));
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessage msg, Integer myId) {
        return ChatMessageResponse.builder()
                .id(msg.getId())
                .content(msg.getContent())
                .sentAt(msg.getSentAt())
                .isRead(msg.getIsRead())
                .isMine(msg.getSender().getId().equals(myId))
                .senderId(msg.getSender().getId())
                .receiverId(msg.getReceiver().getId())
                .build();
    }
}