package com.labback.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Reseña y calificación de un cliente hacia un emprendedor.
 * RF-16: calificación de 1 a 5 estrellas.
 * RF-17: comentario sobre el servicio.
 * RF-18: promedio de calificaciones (se calcula en la capa de servicio).
 * RF-19: emprendedor puede VER pero no modificar reseñas.
 * RN-02: un cliente solo califica si usó el servicio → la FK service_post_id lo garantiza.
 * Constraint único (client_id, service_post_id): un cliente solo puede dejar
 * una reseña por servicio contratado.
 */
@Entity
@Table(
        name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_review_client_service",
                        columnNames = {"client_id", "service_post_id"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // RF-16: calificación entre 1 y 5
    @Column(nullable = false)
    private Integer rating;

    // RF-17: comentario opcional
    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Cliente que escribe la reseña
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    // Emprendedor que recibe la reseña (RF-18, RF-19)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entrepreneur_id", nullable = false)
    private User entrepreneur;

    // Servicio que originó la reseña (RN-02)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_post_id", nullable = false)
    private ServicePost servicePost;
}