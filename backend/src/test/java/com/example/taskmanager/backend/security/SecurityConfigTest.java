package com.example.taskmanager.backend.security;

import com.example.taskmanager.backend.dto.AuthRequestDto;
import com.example.taskmanager.backend.service.AuthService;
import com.example.taskmanager.backend.service.CustomUserDetailsService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.is;

import static org.hamcrest.core.StringRegularExpression.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@TestPropertySource(locations = "classpath:application.properties")
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil; // Для генерации токенов

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService; // Мок для сервиса аутентификации

    @MockBean
    private CustomAuthenticationProvider customAuthenticationProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService; // Мок для UserDetailsService

    private static final String ADMIN_ENDPOINT = "/api/admin/users";

    @BeforeEach
    void setUp() {
        UserDetails userDetails = User.withUsername("test@example.com")
                .password("{bcrypt}$2a$10$GRLdNijBX4UO3H7WIFNvKuwu7zZup7H3Dvn0.3h3k5T2v0E4B7vS")
                .roles("USER")
                .build();
        when(customUserDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);

        when(authService.login(any(AuthRequestDto.class))).thenReturn("mocked-token");
    }

    @Test
    void testSecurityConfig_BeansAreLoaded() {
        assertNotNull(mockMvc);
        assertNotNull(jwtUtil);
        assertNotNull(customUserDetailsService);
    }

    @Test
    void testPublicEndpoints_AccessibleWithoutAuthentication() throws Exception {
        AuthRequestDto loginRequest = new AuthRequestDto();
        loginRequest.setLogin("testUser");
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token", is("mocked-token")));
    }

    @Test
    void testProtectedEndpoints_RequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication failed: Missing or invalid Authorization header"));
    }

    @Test
    void testJwtRequestFilter_IsApplied() throws Exception {
        String token = jwtUtil.generateToken("test@example.com", false);

        mockMvc.perform(get("/api/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("This is a protected endpoint"));
    }

    @Test
    void testJwtRequestFilter_InvalidToken() throws Exception {
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        mockMvc.perform(get("/api/protected")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message", matchesRegex(".*Invalid token.*")));
    }

    @Test
    void testJwtRequestFilter_MissingToken() throws Exception {
        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication failed: Missing or invalid Authorization header"));
    }

    @Test
    void testBlockedUser_CannotAccessProtectedEndpoints() throws Exception {
        // Мокируем UserDetails для заблокированного пользователя
        UserDetails blockedUser = new org.springframework.security.core.userdetails.User(
                "blocked@example.com",
                "password",
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                false, // accountNonLocked (пользователь заблокирован)
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(customUserDetailsService.loadUserByUsername("blocked@example.com")).thenReturn(blockedUser);

        // Генерируем валидный токен для заблокированного пользователя
        String blockedUserToken = jwtUtil.generateToken("blocked@example.com", true);

        // Выполняем запрос с токеном заблокированного пользователя
        mockMvc.perform(get(ADMIN_ENDPOINT)
                        .header("Authorization", "Bearer " + blockedUserToken))
                .andExpect(status().isForbidden()) // Ожидаем 403 Forbidden
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access Denied: account is locked"));
    }

    @Test
    void testNonAdminUser_CannotAccessAdminEndpoints() throws Exception {
        // Мокируем UserDetails для обычного пользователя
        UserDetails regularUser = new org.springframework.security.core.userdetails.User(
                "regular@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")) // Только роль USER
        );

        when(customUserDetailsService.loadUserByUsername("regular@example.com")).thenReturn(regularUser);

        // Генерируем валидный токен для заблокированного пользователя
        String userToken = jwtUtil.generateToken("regular@example.com", false);

        // Выполняем запрос с JWT токеном обычного пользователя
        mockMvc.perform(get(ADMIN_ENDPOINT)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden()) // Ожидаем 403 Forbidden
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

}