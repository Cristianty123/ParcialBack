package com.labback.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Registro de cada imagen subida al servidor.
 *
 * Se usa para dos propósitos:
 *   1. Verificar ownership antes de borrar un archivo (DELETE /images/{filename}).
 *      Solo el usuario que subió la imagen puede borrarla.
 *   2. Poder limpiar archivos huérfanos en el futuro si se necesita una tarea
 *      de mantenimiento (archivos en disco sin referencia en service_images).
 *
 * filename: el UUID + extensión que se usa como nombre de archivo en disco
 *           y como último segmento de la URL pública. Ej: "a1b2c3d4.jpg"
 */
@Entity
@Table(name = "uploaded_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadedImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 255)
    private String filename;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}