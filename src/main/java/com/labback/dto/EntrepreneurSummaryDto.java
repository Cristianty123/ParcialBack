package com.labback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Snapshot del emprendedor para embeber en ServiceDetailResponse (HU-12 CA-1).
 * averageRating proviene de ReviewRepository.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntrepreneurSummaryDto {
    private Integer id;
    private String  fullName;
    private String  photoUrl;
    private Double  averageRating;
}