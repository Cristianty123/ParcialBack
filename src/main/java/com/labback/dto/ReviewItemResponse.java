package com.labback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * HU-19 CA-1: un ítem del listado de reseñas.
 * { rating, comment, createdAt, client: { fullName, photoUrl }, servicePost: { title } }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewItemResponse {
    private Integer       id;
    private Integer       rating;
    private String        comment;
    private LocalDateTime createdAt;
    private ClientDto     client;
    private ServiceTitleDto servicePost;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientDto {
        private String fullName;
        private String photoUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceTitleDto {
        private Integer id;
        private String  title;
    }
}