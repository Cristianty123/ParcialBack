package com.labback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * HU-16 CA-2: un ítem de la lista de conversaciones.
 * { partnerId, partnerName, partnerPhoto, lastMessage, lastMessageAt, unreadCount }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationItemResponse {
    private Integer       partnerId;
    private String        partnerName;
    private String        partnerPhoto;
    private String        lastMessage;
    private LocalDateTime lastMessageAt;
    private Long          unreadCount;
}