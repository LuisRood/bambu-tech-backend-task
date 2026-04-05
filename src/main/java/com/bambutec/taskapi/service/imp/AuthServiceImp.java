package com.bambutec.taskapi.service.imp;

import com.bambutec.taskapi.dto.AuthRequest;
import com.bambutec.taskapi.dto.AuthResponse;
import com.bambutec.taskapi.dto.RegisterRequest;
import com.bambutec.taskapi.dto.InitAdminRequest;
import com.bambutec.taskapi.exception.AuthenticationException;
import com.bambutec.taskapi.exception.ResourceConflictException;
import com.bambutec.taskapi.exception.AdminAlreadyInitializedException;
import com.bambutec.taskapi.model.Role;
import com.bambutec.taskapi.model.entity.User;
import com.bambutec.taskapi.repository.UserRepository;
import com.bambutec.taskapi.security.JwtService;
import com.bambutec.taskapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResourceConflictException("El usuario ya existe");
        }

        var user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER) // Rol por defecto
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder().token(jwtToken).build();
    }

    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new AuthenticationException("Usuario o contraseña incorrectos");
        }

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("Usuario o contraseña incorrectos"));
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder().token(jwtToken).build();
    }

    public AuthResponse initializeFirstAdmin(InitAdminRequest request) {
        // Verifica que no existan usuarios en la base de datos, para crear un administrador para pruebas
        if (!userRepository.findAll().isEmpty()) {
            throw new AdminAlreadyInitializedException("El admin ya fue inicializado, contacte al admistrador para agregar uno nuevo.");
        }

        var adminUser = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .build();
        userRepository.save(adminUser);
        var jwtToken = jwtService.generateToken(adminUser);
        return AuthResponse.builder().token(jwtToken).build();
    }
}