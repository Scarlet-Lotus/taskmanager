package com.example.taskmanager.backend.exception;

import com.example.taskmanager.backend.dto.AuthRequestDto;
import com.example.taskmanager.backend.security.JwtRequestFilter;
import com.example.taskmanager.backend.service.AuthService;
import com.example.taskmanager.backend.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService; // Мок для сервиса аутентификации

    @MockBean
    private CustomUserDetailsService customUserDetailsService; // Мок для UserDetailsService

    @BeforeEach
    void setUp() {
        // Настройка мока для UserDetailsService
        UserDetails userDetails = User.withUsername("test@example.com")
                .password("{bcrypt}$2a$10$GRLdNijBX4UO3H7WIFNvKuwu7zZup7H3Dvn0.3h3k5T2v0E4B7vS") // Хешированный пароль
                .roles("USER")
                .build();
        when(customUserDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
    }

    @Test
    void testHandleRuntimeException() throws Exception {
        when(authService.login(any(AuthRequestDto.class))).thenThrow(new RuntimeException("Test runtime exception"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": \"testUser\", \"email\": \"test@example.com\", \"password\": \"password\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Test runtime exception"))
                .andExpect(jsonPath("$.details").value(containsString("uri=/api/auth/login")));
    }

    @Test
    void testHandleIllegalArgumentException() throws Exception {
        when(authService.login(any(AuthRequestDto.class))).thenThrow(new IllegalArgumentException("Invalid argument"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": \"testUser\", \"email\": \"test@example.com\", \"password\": \"password\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid argument"))
                .andExpect(jsonPath("$.details").value(containsString("uri=/api/auth/login")));
    }

    @Test
    void testHandleNullPointerException() throws Exception {
        when(authService.login(any(AuthRequestDto.class))).thenThrow(new NullPointerException("Null pointer occurred"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": \"testUser\", \"email\": \"test@example.com\", \"password\": \"password\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred: Null pointer occurred"))
                .andExpect(jsonPath("$.details").value(containsString("uri=/api/auth/login")));
    }

    @Test
    void testHandleHttpRequestMethodNotSupportedException() throws Exception {
        mockMvc.perform(get("/api/auth/login")) // Эндпоинт поддерживает только POST
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.message").value(containsString("Method not allowed")))
                .andExpect(jsonPath("$.details").value(containsString("uri=/api/auth/login")));
    }

    @Test
    void testHandleBusinessException() throws Exception {
        when(authService.login(any(AuthRequestDto.class)))
                .thenThrow(new BusinessException("Business logic error"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": \"testUser\", \"email\": \"test@example.com\", \"password\": \"password\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Business logic error"));
    }

    @Test
    void testHandleTechnicalException() throws Exception {
        when(authService.login(any(AuthRequestDto.class)))
                .thenThrow(new TechnicalException("Technical error"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": \"testUser\", \"email\": \"test@example.com\", \"password\": \"password\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Technical error"));
    }

}