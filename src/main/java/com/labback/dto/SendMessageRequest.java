package com.labback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** HU-17 CA-1: body de POST /chat/messages */
@Data
public class SendMessageRequest {

    @NotNull(message = "El destinatario es obligatorio")
    private Integer receiverId;

    @NotBlank(message = "El contenido del mensaje no puede estar vacío")
    private String content;
}