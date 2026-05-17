package com.labback.dto;

import com.labback.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HU-04: respuesta de GET /users/me
 * Expone todos los campos del perfil propio (nunca el password).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Integer id;
    private String  username;
    private Role    role;
    private String  fullName;
    private String  photoUrl;
    private String  description;
    private String  address;
    private Double  latitude;
    private Double  longitude;
}