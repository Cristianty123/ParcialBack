package com.labback.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Imágenes asociadas a un servicio (RF-06).
 * Tabla separada para soportar múltiples imágenes por servicio.
 * imageUrl apuntará a la URL del archivo almacenado
 * (en una etapa futura se puede integrar S3 / Firebase Storage).
 */
@Entity
@Table(name = "service_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_post_id", nullable = false)
    private ServicePost servicePost;
}