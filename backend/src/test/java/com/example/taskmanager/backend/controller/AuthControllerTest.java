package com.example.taskmanager.backend.controller;

import com.example.taskmanager.backend.dto.AuthRequestDto;
import com.example.taskmanager.backend.exception.BusinessException;
import com.example.taskmanager.backend.exception.TechnicalException;
import com.example.taskmanager.backend.security.CustomAuthenticationProvider;
import com.example.taskmanager.backend.security.JwtUtil;
import com.example.taskmanager.backend.security.SecurityConfig;
import com.example.taskmanager.backend.service.AuthService;
import com.example.taskmanager.backend.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class) // Импортируем JwtUtil и конфигурацию безопасности
@ActiveProfiles("test") // Активируем профиль "test" для использования application-test.properties
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomAuthenticationProvider customAuthenticationProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private static final String REGISTER_URL = "/api/auth/register";
    private static final String LOGIN_URL = "/api/auth/login";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testRegister_Success() throws Exception {
        // Arrange
        AuthRequestDto request = new AuthRequestDto("testUser", "test@example.com", "password");
        String token = "mocked-token";
        when(authService.register(any(AuthRequestDto.class))).thenReturn(token);

        // Act & Assert
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));
    }

    @Test
    void testRegister_ValidationError() throws Exception {
        // Arrange
        AuthRequestDto invalidRequest = new AuthRequestDto(null, null, null); // Все поля пустые

        // Act & Assert
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Validation failed")));
    }

    @Test
    void testRegister_BusinessException() throws Exception {
        // Arrange
        AuthRequestDto request = new AuthRequestDto("testUser", "test@example.com", "password");
        when(authService.register(any(AuthRequestDto.class)))
                .thenThrow(new BusinessException("Email already exists"));

        // Act & Assert
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void testRegister_TechnicalException() throws Exception {
        // Arrange
        AuthRequestDto request = new AuthRequestDto("testUser", "test@example.com", "password");
        when(authService.register(any(AuthRequestDto.class)))
                .thenThrow(new TechnicalException("Database connection error"));

        // Act & Assert
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Database connection error"));
    }

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        AuthRequestDto request = new AuthRequestDto("testUser", "test@example.com", "password");
        String token = "mocked-token";
        when(authService.login(any(AuthRequestDto.class))).thenReturn(token);

        // Act & Assert
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));
    }

    @Test
    void testLogin_ValidationError() throws Exception {
        // Arrange
        AuthRequestDto invalidRequest = new AuthRequestDto(null, null, null); // Все поля пустые

        // Act & Assert
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Validation failed")));
    }

    @Test
    void testLogin_BusinessException() throws Exception {
        // Arrange
        AuthRequestDto request = new AuthRequestDto("testUser", "test@example.com", "password");
        when(authService.login(any(AuthRequestDto.class)))
                .thenThrow(new BusinessException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void testLogin_TechnicalException() throws Exception {
        // Arrange
        AuthRequestDto request = new AuthRequestDto("testUser", "test@example.com", "password");
        when(authService.login(any(AuthRequestDto.class)))
                .thenThrow(new TechnicalException("Authentication service unavailable"));

        // Act & Assert
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Authentication service unavailable"));
    }

}