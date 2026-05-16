package com.labback.service;

import com.labback.dto.LoginRequest;
import com.labback.dto.LoginResponse;
import com.labback.dto.RegisterRequest;
import com.labback.exception.UserAlreadyExistsException;
import com.labback.model.User;
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

    /**
     * Autentica un usuario existente.
     * Delega la validación de credenciales a Spring Security (AuthenticationManager),
     * que internamente usa CustomUserDetailService + BCrypt.
     * Si las credenciales son incorrectas, AuthenticationManager lanza una excepción
     * que el AuthController captura y convierte en 401.
     */
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        // 1. Validar credenciales (lanza excepción si falla)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // 2. Establecer autenticación en el contexto de Spring Security
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Buscar usuario en BD para obtener el rol y generar el token
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Error interno: usuario no encontrado tras autenticación."));

        // 4. Generar JWT (el rol se incluye como claim dentro del token)
        String token = jwtTokenService.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())     // Flutter necesita el rol para navegar al home correcto
                .message("Login exitoso")
                .success(true)
                .build();
    }

    /**
     * Registra un nuevo usuario con el rol indicado.
     * Lanza UserAlreadyExistsException si el username ya existe.
     */
    public LoginResponse register(RegisterRequest request) {
        // 1. Verificar unicidad del username
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("El nombre de usuario '" + request.getUsername() + "' ya está en uso");
        }

        // 2. Construir la entidad con el rol recibido del cliente
        User nuevoUsuario = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())   // CLIENT o ENTREPRENEUR
                .build();

        // 3. Persistir
        User savedUser = userRepository.save(nuevoUsuario);

        // 4. Generar token y devolver respuesta lista para que Flutter almacene la sesión
        String token = jwtTokenService.generateToken(savedUser);

        return LoginResponse.builder()
                .token(token)
                .username(savedUser.getUsername())
                .role(savedUser.getRole())
                .success(true)
                .message("Usuario registrado exitosamente")
                .build();
    }
}