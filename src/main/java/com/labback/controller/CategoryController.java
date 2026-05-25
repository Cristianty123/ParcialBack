package com.labback.controller;

import com.labback.dto.CategoryDto;
import com.labback.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HU-15 — Listado de categorías.
 * GET /categories
 * Endpoint público: no requiere JWT (HU-15 CA-2).
 * Hay que agregar /categories a la lista de rutas públicas en SecurityConfig.
 */
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final SearchService searchService;

    public CategoryController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Devuelve todas las categorías ordenadas alfabéticamente.
     * Respuesta: [{ id, name }, ...]
     */
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(searchService.getAllCategories());
    }
}