package com.labback.controller;

import com.labback.dto.LoginRequest;
import com.labback.dto.LoginResponse;
import com.labback.dto.RegisterRequest;
import com.labback.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/authenticate")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * HU-02 — Inicio de sesión.
     * Devuelve token JWT + username + role para que Flutter
     * sepa a qué pantalla navegar (home cliente vs dashboard emprendedor).
     * POST /authenticate/login
     * Body: { "username": "...", "password": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Cubre BadCredentialsException y cualquier error de autenticación.
            // El mensaje es genérico a propósito (no revelar si el usuario existe o no).
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    LoginResponse.builder()
                            .success(false)
                            .message("Usuario o contraseña incorrectos")
                            .build()
            );
        }
    }

    /**
     * HU-01 — Registro de usuario.
     * El body debe incluir role: "CLIENT" o "ENTREPRENEUR".
     * La anotación @Valid activa las validaciones de RegisterRequest (NotBlank, Size, NotNull).
     * POST /authenticate/register
     * Body: { "username": "...", "password": "...", "role": "CLIENT" }
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            LoginResponse response = authService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            // Cubre UserAlreadyExistsException y cualquier error de negocio
            return ResponseEntity.badRequest().body(
                    LoginResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    /**
     * Captura los errores de validación de @Valid y los convierte
     * en una respuesta 400 con los mensajes de cada campo fallido.
     * Sin este handler, Spring devuelve un JSON de error genérico poco legible.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<LoginResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().body(
                LoginResponse.builder()
                        .success(false)
                        .message(errorMessage)
                        .build()
        );
    }
}