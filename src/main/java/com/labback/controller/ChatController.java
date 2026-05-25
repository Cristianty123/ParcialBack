package com.labback.controller;

import com.labback.dto.ChatMessageResponse;
import com.labback.dto.ConversationItemResponse;
import com.labback.dto.PagedResponse;
import com.labback.dto.SendMessageRequest;
import com.labback.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * HU-16 — Listar conversaciones del usuario autenticado.
     * GET /chat/conversations
     * Devuelve lista ordenada por lastMessageAt DESC.
     * Cada ítem: { partnerId, partnerName, partnerPhoto,
     *              lastMessage, lastMessageAt, unreadCount }
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationItemResponse>> getConversations(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<ConversationItemResponse> conversations =
                chatService.getConversations(userDetails.getUsername());
        return ResponseEntity.ok(conversations);
    }

    /**
     * HU-17 — Enviar un mensaje.
     * POST /chat/messages
     * Body: { receiverId, content }
     * Respuesta 201 con el mensaje creado.
     */
    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SendMessageRequest request) {

        try {
            ChatMessageResponse message =
                    chatService.sendMessage(userDetails.getUsername(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }

    /**
     * HU-17 — Historial paginado de mensajes con un usuario.
     * GET /chat/messages/{partnerId}?page=0&size=30
     * Ordenado por sentAt DESC (mensajes más recientes primero).
     * HU-17 CA-4: la query filtra por (userId, partnerId) implícitamente,
     * así que un usuario solo ve sus propias conversaciones.
     */
    @GetMapping("/messages/{partnerId}")
    public ResponseEntity<?> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer partnerId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "30") int size) {

        try {
            PagedResponse<ChatMessageResponse> history =
                    chatService.getHistory(userDetails.getUsername(), partnerId, page, size);
            return ResponseEntity.ok(history);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * HU-17 — Marcar mensajes de un partner como leídos.
     * PATCH /chat/messages/read/{partnerId}
     * Marca como leídos todos los mensajes que partnerId envió al usuario autenticado.
     * Respuesta 200 con el número de mensajes actualizados.
     *
     * IMPORTANTE: esta ruta debe declararse ANTES de GET /messages/{partnerId}
     * para que Spring no interprete "read" como un partnerId.
     */
    @PatchMapping("/messages/read/{partnerId}")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer partnerId) {

        int updated = chatService.markAsRead(userDetails.getUsername(), partnerId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "messagesMarkedAsRead", updated));
    }

    // ---------------------------------------------------------------
    // Manejo de errores de validación (@Valid)
    // ---------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(errorBody(message));
    }

    private Map<String, Object> errorBody(String message) {
        return Map.of("success", false, "message", message);
    }
}