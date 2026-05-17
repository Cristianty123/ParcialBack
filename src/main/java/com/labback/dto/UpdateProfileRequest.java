package com.labback.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * HU-05: body de PUT /users/me
 * Todos los campos son opcionales; null = no modificar.
 * username y password NO están aquí a propósito (HU-05 CA-4).
 */
@Data
public class UpdateProfileRequest {

    @Size(max = 100, message = "El nombre completo no puede superar los 100 caracteres")
    private String fullName;

    private String photoUrl;

    private String description;

    @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
    private String address;

    private Double latitude;
    private Double longitude;
}