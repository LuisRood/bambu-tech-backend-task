package com.bambutec.taskapi.service;

import com.bambutec.taskapi.dto.AuthRequest;
import com.bambutec.taskapi.dto.AuthResponse;
import com.bambutec.taskapi.dto.RegisterRequest;

public interface AuthService {
    public AuthResponse register(RegisterRequest request);

    public AuthResponse login(AuthRequest request);
}
