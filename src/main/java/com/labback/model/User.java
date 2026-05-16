package com.labback.model;

import com.labback.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad principal de usuario.
 * Un usuario puede ser CLIENT (busca servicios) o ENTREPRENEUR (publica servicios).
 * Los campos de perfil (fullName, photoUrl, description, ubicación) son opcionales
 * y se llenan después del registro.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // --- Campos de perfil (RF-03) ---

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Ubicación del usuario (para geolocalización)
    private Double latitude;

    private Double longitude;

    private String address;
}