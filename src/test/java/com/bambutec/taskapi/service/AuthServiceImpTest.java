package com.bambutec.taskapi.service;

import com.bambutec.taskapi.dto.AuthRequest;
import com.bambutec.taskapi.dto.RegisterRequest;
import com.bambutec.taskapi.exception.AuthenticationException;
import com.bambutec.taskapi.exception.ResourceConflictException;
import com.bambutec.taskapi.model.Role;
import com.bambutec.taskapi.model.entity.User;
import com.bambutec.taskapi.repository.UserRepository;
import com.bambutec.taskapi.security.JwtService;
import com.bambutec.taskapi.service.imp.AuthServiceImp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImpTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImp authServiceImp;

    @Test
    void register_WhenUsernameAlreadyExists_ShouldThrowConflict() {
        RegisterRequest request = RegisterRequest.builder()
                .username("existing")
                .password("password123")
                .build();

        when(userRepository.findByUsername("existing")).thenReturn(Optional.of(User.builder().build()));

        assertThrows(ResourceConflictException.class, () -> authServiceImp.register(request));
    }

    @Test
    void register_WhenValidRequest_ShouldReturnJwtToken() {
        RegisterRequest request = RegisterRequest.builder()
                .username("new-user")
                .password("password123")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .username("new-user")
                .password("encoded-pass")
                .role(Role.USER)
                .build();

        when(userRepository.findByUsername("new-user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        var response = authServiceImp.register(request);

        assertEquals("jwt-token", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_WhenCredentialsAreInvalid_ShouldThrowAuthenticationException() {
        AuthRequest request = AuthRequest.builder()
                .username("user")
                .password("wrong-pass")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(AuthenticationException.class, () -> authServiceImp.login(request));
    }
}
