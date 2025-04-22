package com.example.taskmanager.backend.controller;

import com.example.taskmanager.backend.dto.UserDto;
import com.example.taskmanager.backend.dto.UserRegistrationDto;
import com.example.taskmanager.backend.exception.GlobalExceptionHandler;
import com.example.taskmanager.backend.exception.UserNotFoundException;
import com.example.taskmanager.backend.security.JwtRequestFilter;
import com.example.taskmanager.backend.security.JwtUtil;
import com.example.taskmanager.backend.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@Import(GlobalExceptionHandler.class) // Подключаем глобальный обработчик исключений
@AutoConfigureMockMvc(addFilters = false) // Отключаем фильтры безопасности
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long USER_ID = 1L;
    private static final UserRegistrationDto SAMPLE_REGISTRATION_DTO = new UserRegistrationDto("testLogin", "test@example.com", "password");
    private static final UserDto SAMPLE_USER_DTO = new UserDto(USER_ID, "testLogin", "test@example.com", "USER");

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testUserDtoSerialization() throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(SAMPLE_USER_DTO);
        System.out.println("Serialized UserDto: " + json);

        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"login\":\"testLogin\""));
        assertTrue(json.contains("\"email\":\"test@example.com\""));
        assertTrue(json.contains("\"role\":\"USER\""));
    }

    @Test
    void testCreateUser_Success() throws Exception {
        when(userService.register(any(UserRegistrationDto.class))).thenReturn(SAMPLE_USER_DTO);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(SAMPLE_REGISTRATION_DTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.login").value("testLogin"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).register(any(UserRegistrationDto.class));
    }

    @Test
    void testCreateUser_ValidationError() throws Exception {
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setLogin(""); // Некорректное значение

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed: Email is required, Login is required, Password is required"));
    }

    @Test
    void testGetAllUsers_Success() throws Exception {
        List<UserDto> users = List.of(SAMPLE_USER_DTO);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(USER_ID))
                .andExpect(jsonPath("$[0].email").value("test@example.com"))
                .andExpect(jsonPath("$[0].login").value("testLogin"))
                .andExpect(jsonPath("$[0].role").value("USER"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testBlockUser_Success() throws Exception {
        doNothing().when(userService).blockUser(USER_ID);

        mockMvc.perform(post("/api/admin/users/{userId}/block", USER_ID))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).blockUser(USER_ID);
    }

    @Test
    void testBlockUser_UserNotFound() throws Exception {
        doThrow(new UserNotFoundException("User not found")).when(userService).blockUser(USER_ID);

        mockMvc.perform(post("/api/admin/users/{userId}/block", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userService, times(1)).blockUser(USER_ID);
    }

    @Test
    void testUnblockUser_Success() throws Exception {
        doNothing().when(userService).unblockUser(USER_ID);

        mockMvc.perform(post("/api/admin/users/{userId}/unblock", USER_ID))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).unblockUser(USER_ID);
    }

    @Test
    void testUnblockUser_UserNotFound() throws Exception {
        doThrow(new UserNotFoundException("User not found")).when(userService).unblockUser(USER_ID);

        mockMvc.perform(post("/api/admin/users/{userId}/unblock", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userService, times(1)).unblockUser(USER_ID);
    }

    @Test
    void testMakeAdmin_Success() throws Exception {
        doNothing().when(userService).makeAdmin(USER_ID);

        mockMvc.perform(post("/api/admin/users/{userId}/make-admin", USER_ID))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).makeAdmin(USER_ID);
    }

    @Test
    void testMakeAdmin_UserNotFound() throws Exception {
        doThrow(new UserNotFoundException("User not found")).when(userService).makeAdmin(USER_ID);

        mockMvc.perform(post("/api/admin/users/{userId}/make-admin", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userService, times(1)).makeAdmin(USER_ID);
    }

    @Test
    void testMakeUser_Success() throws Exception {
        doNothing().when(userService).makeUser(USER_ID);

        mockMvc.perform(post("/api/admin/users/{userId}/make-user", USER_ID))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).makeUser(USER_ID);
    }

    @Test
    void testMakeUser_UserNotFound() throws Exception {
        doThrow(new UserNotFoundException("User not found")).when(userService).makeUser(USER_ID);

        mockMvc.perform(post("/api/admin/users/{userId}/make-user", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userService, times(1)).makeUser(USER_ID);
    }
}