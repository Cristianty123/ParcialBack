package com.labback.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> healthCheck() {
        // Devolvemos un Map para que Spring lo convierta automáticamente a JSON
        return Map.of(
                "status", "alive",
                "message", "El servidor de LabBack está funcionando correctamente"
        );
    }
}