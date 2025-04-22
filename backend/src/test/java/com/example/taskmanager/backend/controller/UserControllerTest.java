package com.example.taskmanager.backend.controller;

import com.example.taskmanager.backend.dto.TaskDto;
import com.example.taskmanager.backend.dto.UserProfileDto;
import com.example.taskmanager.backend.dto.UserUpdateDto;
import com.example.taskmanager.backend.security.JwtUtil;
import com.example.taskmanager.backend.service.CustomUserDetailsService;
import com.example.taskmanager.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
@ActiveProfiles("test") // Активируем профиль "test" для использования application-test.properties
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private static final String PROFILE_URL = "/api/users/profile";
    private static final String UPDATE_URL = "/api/users/update";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testGetProfile_Success() throws Exception {
        // Arrange
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email, false); // Генерация валидного токена
        UserProfileDto profileDto = new UserProfileDto(
                1L,
                "testUser",
                email,
                "firstName",
                "lastName",
                "USER",
                List.of(new TaskDto(1L, "Task 1", "Description", "TO_DO", "LOW", LocalDate.now().plusDays(1), 1L))
        );
        when(userService.getUserProfile(email)).thenReturn(profileDto);

        // Act & Assert
        mockMvc.perform(get(PROFILE_URL)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(email).roles("USER"))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.login").value("testUser"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.tasks[0].id").value(1L))
                .andExpect(jsonPath("$.tasks[0].title").value("Task 1"));
    }


    @Test
    void testGetProfile_UserNotFound() throws Exception {
        String email = "nonexistent@example.com";
        String token = jwtUtil.generateToken(email, false); // Генерация валидного JWT-токена
        when(userService.getUserProfile(email)).thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get(PROFILE_URL)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()) // Добавляем CSRF-токен
                        .with(SecurityMockMvcRequestPostProcessors.user(email).roles("USER")) // Эмулируем пользователя с ролью USER
                        .header("Authorization", "Bearer " + token)) // Добавляем JWT-токен
                .andExpect(status().isNotFound()) // Ожидаем статус 404
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void testGetProfile_Unauthorized() throws Exception {
        mockMvc.perform(get(PROFILE_URL))
                .andExpect(status().isUnauthorized()) // Ожидаем статус 401
                .andExpect(content().string("")); // Ожидаем пустое тело ответа
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        // Arrange
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email, false); // Генерация валидного токена
        UserUpdateDto updateDto = new UserUpdateDto(
                "newLogin",
                "newPassword",
                "newFirstName",
                "newLastName"
        );
        UserProfileDto updatedProfileDto = new UserProfileDto(
                1L,
                "newLogin",
                email,
                "newFirstName",
                "newLastName",
                "USER",
                List.of(new TaskDto(1L, "Task 1", "Description", "TO_DO", "LOW", LocalDate.now().plusDays(1), 1L))
        );
        when(userService.updateUser(eq(email), any(UserUpdateDto.class))).thenReturn(updatedProfileDto);

        // Act & Assert
        mockMvc.perform(put(UPDATE_URL)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(email).roles("USER"))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.login").value("newLogin"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.tasks[0].id").value(1L))
                .andExpect(jsonPath("$.tasks[0].title").value("Task 1"));

        verify(userService, times(1)).updateUser(eq(email), any(UserUpdateDto.class));
    }

    @Test
    void testUpdateUser_ValidationError() throws Exception {
        // Arrange
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email, false); // Генерация валидного токена
        UserUpdateDto invalidUpdateDto = new UserUpdateDto("", "", "", ""); // Некорректные данные

        // Act & Assert
        mockMvc.perform(put(UPDATE_URL)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()) // Добавляем CSRF-токен
                        .with(SecurityMockMvcRequestPostProcessors.user(email).roles("USER")) // Эмулируем пользователя с ролью USER
                        .header("Authorization", "Bearer " + token) // Добавляем JWT-токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Validation failed")));
    }

}