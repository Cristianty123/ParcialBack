package com.labback.controller;

import com.labback.dto.LoginRequest;
import com.labback.dto.LoginResponse;
import com.labback.dto.RegisterRequest;
import com.labback.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/authenticate")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.authenticateUser(loginRequest);
            response.setSuccess(true);
            response.setMessage("Login exitoso");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LoginResponse errorResponse = LoginResponse.builder()
                    .success(false)
                    .message("Usuario o contraseña incorrectos")
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest registerRequest) {
        try {
            LoginResponse response = authService.register(registerRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    LoginResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

}
