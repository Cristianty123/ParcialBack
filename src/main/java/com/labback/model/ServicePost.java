package com.labback.model;

import com.labback.enums.ServiceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio publicado por un emprendedor.
 * RF-05: publicar servicio.
 * RF-06: nombre, descripción, categoría, precio (opcional), imágenes, ubicación.
 * RF-07: editar / eliminar.
 * RF-08: estado activo / inactivo.
 * RF-11: geolocalización en mapa.
 * Nota: el nombre de clase es ServicePost (no "Service") para evitar
 * conflicto con la anotación @Service de Spring.
 */
@Entity
@Table(name = "service_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // Precio opcional (RF-06)
    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    // Ubicación del servicio (RF-06, RF-11)
    private Double latitude;

    private Double longitude;

    private String address;

    // Estado activo/inactivo (RF-08)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ServiceStatus status = ServiceStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- Relaciones ---

    // El emprendedor dueño del servicio (RF-05)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entrepreneur_id", nullable = false)
    private User entrepreneur;

    // Categoría del servicio (RF-06, RF-09)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Imágenes del servicio (RF-06)
    @OneToMany(mappedBy = "servicePost", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ServiceImage> images = new ArrayList<>();
}