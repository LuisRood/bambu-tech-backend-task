package com.bambutec.taskapi.service;

import com.bambutec.taskapi.dto.AuthRequest;
import com.bambutec.taskapi.dto.InitAdminRequest;
import com.bambutec.taskapi.dto.RegisterRequest;
import com.bambutec.taskapi.exception.AdminAlreadyInitializedException;
import com.bambutec.taskapi.exception.AuthenticationException;
import com.bambutec.taskapi.exception.ResourceConflictException;
import com.bambutec.taskapi.model.Role;
import com.bambutec.taskapi.model.entity.User;
import com.bambutec.taskapi.repository.UserRepository;
import com.bambutec.taskapi.security.JwtService;
import com.bambutec.taskapi.service.imp.AuthServiceImp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

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

    @Test
    void login_WhenCredentialsAreValid_ShouldReturnJwtToken() {
        AuthRequest request = AuthRequest.builder()
                .username("user")
                .password("password123")
                .build();

        User user = User.builder()
                .id(10L)
                .username("user")
                .password("encoded")
                .role(Role.USER)
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-login-token");

        var response = authServiceImp.login(request);

        assertEquals("jwt-login-token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void initializeFirstAdmin_WhenUsersAlreadyExist_ShouldThrowException() {
        InitAdminRequest request = InitAdminRequest.builder()
                .username("admin")
                .password("admin123456")
                .build();

        when(userRepository.findAll()).thenReturn(List.of(User.builder().id(1L).build()));

        assertThrows(AdminAlreadyInitializedException.class, () -> authServiceImp.initializeFirstAdmin(request));
    }

    @Test
    void initializeFirstAdmin_WhenNoUsersExist_ShouldCreateAdminAndReturnToken() {
        InitAdminRequest request = InitAdminRequest.builder()
                .username("admin")
                .password("admin123456")
                .build();

        User savedAdmin = User.builder()
                .id(99L)
                .username("admin")
                .password("encoded-admin-pass")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findAll()).thenReturn(List.of());
        when(passwordEncoder.encode("admin123456")).thenReturn("encoded-admin-pass");
        when(userRepository.save(any(User.class))).thenReturn(savedAdmin);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-admin-token");

        var response = authServiceImp.initializeFirstAdmin(request);

        assertEquals("jwt-admin-token", response.getToken());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(Role.ADMIN, captor.getValue().getRole());
        assertEquals("admin", captor.getValue().getUsername());
    }
}

