package com.labback.repository;

import com.labback.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    // ---------------------------------------------------------------
    // HU-16: lista de conversaciones del usuario autenticado
    //
    // Devuelve el ID del último mensaje de cada conversación en la que
    // participa el usuario (como sender o receiver).
    // El "canal" entre dos usuarios se identifica por el par ordenado
    // LEAST(sender_id, receiver_id) / GREATEST(sender_id, receiver_id),
    // tal como está documentado en el modelo ChatMessage.
    //
    // Se usa nativeQuery porque JPQL no tiene LEAST/GREATEST.
    // ---------------------------------------------------------------

    @Query(value = """
            SELECT DISTINCT ON (LEAST(m.sender_id, m.receiver_id), GREATEST(m.sender_id, m.receiver_id))
                   m.id
            FROM chat_messages m
            WHERE m.sender_id = :userId OR m.receiver_id = :userId
            ORDER BY LEAST(m.sender_id, m.receiver_id),
                     GREATEST(m.sender_id, m.receiver_id),
                     m.sent_at DESC
            """, nativeQuery = true)
    List<Integer> findLastMessageIdPerConversation(@Param("userId") Integer userId);

    /**
     * Carga los mensajes por sus IDs para construir los items de la bandeja.
     * Se hace en dos pasos (IDs → entidades) para poder usar JPQL con FETCH
     * y evitar N+1 al acceder a sender/receiver.
     */
    @Query("""
            SELECT m FROM ChatMessage m
            JOIN FETCH m.sender
            JOIN FETCH m.receiver
            WHERE m.id IN :ids
            ORDER BY m.sentAt DESC
            """)
    List<ChatMessage> findByIdsWithUsers(@Param("ids") List<Integer> ids);

    // ---------------------------------------------------------------
    // HU-16: conteo de mensajes no leídos por conversación
    // ---------------------------------------------------------------

    @Query("""
            SELECT COUNT(m) FROM ChatMessage m
            WHERE m.receiver.id = :receiverId
              AND m.sender.id   = :senderId
              AND m.isRead      = false
            """)
    Long countUnread(
            @Param("receiverId") Integer receiverId,
            @Param("senderId")   Integer senderId
    );

    // ---------------------------------------------------------------
    // HU-17: historial paginado entre dos usuarios
    //
    // Devuelve mensajes donde (sender=A y receiver=B) o (sender=B y receiver=A),
    // ordenados por sentAt DESC para mostrar los más recientes primero.
    // ---------------------------------------------------------------

    @Query("""
            SELECT m FROM ChatMessage m
            JOIN FETCH m.sender
            JOIN FETCH m.receiver
            WHERE (m.sender.id   = :userId AND m.receiver.id = :partnerId)
               OR (m.sender.id   = :partnerId AND m.receiver.id = :userId)
            ORDER BY m.sentAt DESC
            """)
    Page<ChatMessage> findConversationHistory(
            @Param("userId")    Integer userId,
            @Param("partnerId") Integer partnerId,
            Pageable            pageable
    );

    // ---------------------------------------------------------------
    // HU-17 CA-3: marcar como leídos todos los mensajes de partnerId → userId
    // ---------------------------------------------------------------

    @Modifying
    @Query("""
            UPDATE ChatMessage m
            SET m.isRead = true
            WHERE m.sender.id   = :senderId
              AND m.receiver.id = :receiverId
              AND m.isRead      = false
            """)
    int markAsRead(
            @Param("senderId")   Integer senderId,
            @Param("receiverId") Integer receiverId
    );
}