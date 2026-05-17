package com.labback.service;

import com.labback.dto.EntrepreneurProfileResponse;
import com.labback.dto.UpdateProfileRequest;
import com.labback.dto.UserProfileResponse;
import com.labback.enums.Role;
import com.labback.model.User;
import com.labback.repository.ReviewRepository;
import com.labback.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository   userRepository;
    private final ReviewRepository reviewRepository;

    public UserService(UserRepository userRepository, ReviewRepository reviewRepository) {
        this.userRepository   = userRepository;
        this.reviewRepository = reviewRepository;
    }

    // ---------------------------------------------------------------
    // HU-04: GET /users/me — perfil propio
    // ---------------------------------------------------------------

    /**
     * Devuelve el perfil completo del usuario autenticado.
     * El username viene del subject del JWT, extraído por el controller.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getOwnProfile(String username) {
        User user = findByUsernameOrThrow(username);
        return toUserProfileResponse(user);
    }

    // ---------------------------------------------------------------
    // HU-05: PUT /users/me — editar perfil
    // ---------------------------------------------------------------

    /**
     * Actualización parcial: solo sobreescribe campos no nulos en el request.
     * username y password quedan intactos (HU-05 CA-4).
     */
    @Transactional
    public UserProfileResponse updateOwnProfile(String username, UpdateProfileRequest req) {
        User user = findByUsernameOrThrow(username);

        // Actualización parcial: null = conservar valor existente
        if (req.getFullName()    != null) user.setFullName(req.getFullName());
        if (req.getPhotoUrl()    != null) user.setPhotoUrl(req.getPhotoUrl());
        if (req.getDescription() != null) user.setDescription(req.getDescription());
        if (req.getAddress()     != null) user.setAddress(req.getAddress());
        if (req.getLatitude()    != null) user.setLatitude(req.getLatitude());
        if (req.getLongitude()   != null) user.setLongitude(req.getLongitude());

        User saved = userRepository.save(user);
        return toUserProfileResponse(saved);
    }

    // ---------------------------------------------------------------
    // HU-06: GET /users/{id} — perfil público de emprendedor
    // ---------------------------------------------------------------

    /**
     * Devuelve el perfil público de un emprendedor.
     * Lanza IllegalArgumentException si el id no existe o no es ENTREPRENEUR.
     */
    @Transactional(readOnly = true)
    public EntrepreneurProfileResponse getEntrepreneurProfile(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));

        // HU-06 CA-2: solo se exponen perfiles de emprendedores
        if (user.getRole() != Role.ENTREPRENEUR) {
            throw new IllegalArgumentException("El usuario con id " + id + " no es un emprendedor");
        }

        Double avgRating   = reviewRepository.findAverageRatingByEntrepreneurId(id);
        Long   totalReviews = reviewRepository.countByEntrepreneurId(id);

        // Redondear el promedio a 1 decimal si existe
        Double roundedAvg = (avgRating != null)
                ? Math.round(avgRating * 10.0) / 10.0
                : null;

        return EntrepreneurProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .photoUrl(user.getPhotoUrl())
                .description(user.getDescription())
                .address(user.getAddress())
                .averageRating(roundedAvg)
                .totalReviews(totalReviews)
                .build();
    }

    // ---------------------------------------------------------------
    // Helpers privados
    // ---------------------------------------------------------------

    private User findByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    private UserProfileResponse toUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .fullName(user.getFullName())
                .photoUrl(user.getPhotoUrl())
                .description(user.getDescription())
                .address(user.getAddress())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .build();
    }
}