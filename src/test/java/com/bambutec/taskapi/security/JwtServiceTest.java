package com.bambutec.taskapi.security;

import com.bambutec.taskapi.model.Role;
import com.bambutec.taskapi.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "YmFtYnUtdGFzay1hcGktc2VjcmV0LWtleS0yMDI2LXN1cGVyLXN0cm9uZw==");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 60_000L);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        User user = User.builder()
                .id(1L)
                .username("john")
                .password("encoded")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertEquals("john", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));
    }
}
