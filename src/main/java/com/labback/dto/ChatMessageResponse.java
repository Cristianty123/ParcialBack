package com.labback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * HU-17 CA-2: un mensaje dentro del historial de conversación.
 * Flutter usa isMine para saber si renderizar la burbuja a izquierda o derecha.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Integer       id;
    private String        content;
    private LocalDateTime sentAt;
    private Boolean       isRead;
    private Boolean       isMine;       // true si el sender es el usuario autenticado
    private Integer       senderId;
    private Integer       receiverId;
}