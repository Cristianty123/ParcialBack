package com.labback.service;

import com.labback.dto.LoginRequest;
import com.labback.dto.LoginResponse;
import com.labback.dto.RegisterRequest;
import com.labback.exception.UserAlreadyExistsException;
import com.labback.model.Users;
import com.labback.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       AuthenticationManager authenticationManager,
                       JwtTokenService jwtTokenService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        // 1. Delegar la validación al AuthenticationManager
        // Esto lanzará una excepción automáticamente si las credenciales fallan
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // 2. Establecer la autenticación en el contexto de Spring
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Buscar al usuario en la DB para obtener sus datos y generar el token
        Users user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado tras autenticación."));

        // 4. Generar el String del JWT
        String token = jwtTokenService.generateToken(user);

        // 5. Retornar el DTO con el token y el nombre para el móvil
        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .message("Login exitoso")
                .success(true)
                .build();
    }

    public LoginResponse register(RegisterRequest registro) {
        // 1. Verificar si el usuario ya existe
        if (userRepository.findByUsername(registro.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("El nombre de usuario ya está en uso");
        }

        // 2. Mapear el DTO a la Entidad (Convertir LoginRequest -> Users)
        Users nuevoUsuario = new Users();
        nuevoUsuario.setUsername(registro.getUsername());

        // 3. Encriptar la contraseña
        String encodedPassword = passwordEncoder.encode(registro.getPassword());
        nuevoUsuario.setPassword(encodedPassword);

        // 4. Guardar la ENTIDAD en PostgreSQL
        Users savedUser = userRepository.save(nuevoUsuario);

        // 5. Generar token
        String token = jwtTokenService.generateToken(savedUser);

        return LoginResponse.builder()
                .token(token)
                .username(savedUser.getUsername())
                .success(true)
                .message("Usuario registrado exitosamente")
                .build();
    }
}