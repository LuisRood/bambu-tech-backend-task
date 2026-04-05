package com.bambutec.taskapi.controller;

import com.bambutec.taskapi.dto.AuthRequest;
import com.bambutec.taskapi.dto.AuthResponse;
import com.bambutec.taskapi.dto.RegisterRequest;
import com.bambutec.taskapi.dto.InitAdminRequest;
import com.bambutec.taskapi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/init-admin")
    public ResponseEntity<AuthResponse> initializeFirstAdmin(@Valid @RequestBody InitAdminRequest request) {
        return ResponseEntity.ok(authService.initializeFirstAdmin(request));
    }
}