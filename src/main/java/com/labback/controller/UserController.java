package com.labback.controller;

import com.labback.dto.EntrepreneurProfileResponse;
import com.labback.dto.UpdateProfileRequest;
import com.labback.dto.UserProfileResponse;
import com.labback.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * HU-04 — Ver perfil propio.
     * GET /users/me
     * Requiere JWT válido. El usuario se identifica por el token, no por la URL.
     * Respuesta 200: { id, username, role, fullName, photoUrl, description, address, latitude, longitude }
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getOwnProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        UserProfileResponse profile = userService.getOwnProfile(userDetails.getUsername());
        return ResponseEntity.ok(profile);
    }

    /**
     * HU-05 — Editar perfil.
     * PUT /users/me
     * Campos opcionales: fullName, photoUrl, description, address, latitude, longitude.
     * Los campos no enviados (null) no sobreescriben los existentes.
     * username y password NO se actualizan por este endpoint (CA-4).
     * Respuesta 200: perfil actualizado.
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateOwnProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {

        UserProfileResponse updated = userService.updateOwnProfile(
                userDetails.getUsername(), request);
        return ResponseEntity.ok(updated);
    }

    /**
     * HU-06 — Ver perfil público de un emprendedor.
     * GET /users/{id}
     * Solo devuelve perfiles de usuarios con role = ENTREPRENEUR (CA-2).
     * Incluye averageRating y totalReviews calculados en tiempo real.
     * Respuesta 200: { id, fullName, photoUrl, description, address, averageRating, totalReviews }
     * Respuesta 404: si el id no existe o no es ENTREPRENEUR.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntrepreneurProfileResponse> getEntrepreneurProfile(
            @PathVariable Integer id) {

        try {
            EntrepreneurProfileResponse profile = userService.getEntrepreneurProfile(id);
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}