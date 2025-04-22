package com.example.taskmanager.backend.controller;

import com.example.taskmanager.backend.dto.CommentDto;
import com.example.taskmanager.backend.dto.CommentRequestDto;
import com.example.taskmanager.backend.exception.CommentNotFoundException;
import com.example.taskmanager.backend.exception.GlobalExceptionHandler;
import com.example.taskmanager.backend.exception.TaskNotFoundException;
import com.example.taskmanager.backend.exception.UserNotFoundException;
import com.example.taskmanager.backend.security.JwtRequestFilter;
import com.example.taskmanager.backend.security.JwtUtil;
import com.example.taskmanager.backend.service.CommentService;
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

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@Import(GlobalExceptionHandler.class) // Импортируем глобальный обработчик
@AutoConfigureMockMvc(addFilters = false) // Отключаем фильтры безопасности
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long TASK_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final String USER_NAME = "test_user";
    private static final Long COMMENT_ID = 3L;
    private static final LocalDateTime CREATED_AT = LocalDateTime.now();
    private static final String CONTENT = "This is a test comment";
    private static final String UPDATED_CONTENT = "Updated comment content";

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    // Тест: Создание нового комментария
    @Test
    void testCreateComment_Success() throws Exception {
        CommentRequestDto requestDto = new CommentRequestDto(CONTENT);
        CommentDto dto = new CommentDto(COMMENT_ID, TASK_ID, USER_ID, USER_NAME, CONTENT, CREATED_AT);

        when(commentService.createComment(eq(TASK_ID), eq(USER_ID), eq(CONTENT))).thenReturn(dto);

        mockMvc.perform(post("/api/comments")
                        .param("taskId", String.valueOf(TASK_ID))
                        .param("userId", String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(COMMENT_ID))
                .andExpect(jsonPath("$.content").value(CONTENT));

        verify(commentService, times(1)).createComment(eq(TASK_ID), eq(USER_ID), eq(CONTENT));
    }

    // Тест: Создание нового комментария с несуществующим пользователем
    @Test
    void testCreateComment_NonExistentUser() throws Exception {
        CommentRequestDto requestDto = new CommentRequestDto(CONTENT);

        when(commentService.createComment(eq(TASK_ID), eq(USER_ID), eq(CONTENT)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/api/comments")
                        .param("taskId", String.valueOf(TASK_ID))
                        .param("userId", String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(commentService, times(1)).createComment(eq(TASK_ID), eq(USER_ID), eq(CONTENT));
    }

    // Тест: Создание нового комментария c несуществующей задачей
    @Test
    void testCreateComment_NonExistentTask() throws Exception {
        CommentRequestDto requestDto = new CommentRequestDto(CONTENT);

        when(commentService.createComment(eq(TASK_ID), eq(USER_ID), eq(CONTENT)))
                .thenThrow(new TaskNotFoundException("Task not found"));

        mockMvc.perform(post("/api/comments")
                        .param("taskId", String.valueOf(TASK_ID))
                        .param("userId", String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found"));

        verify(commentService, times(1)).createComment(eq(TASK_ID), eq(USER_ID), eq(CONTENT));
    }

    // Тест: создание нового комментария с пустым контентом
    @Test
    void testCreateComment_EmptyContent() throws Exception {
        CommentRequestDto requestDto = new CommentRequestDto("");

        mockMvc.perform(post("/api/comments")
                        .param("taskId", String.valueOf(TASK_ID))
                        .param("userId", String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed: content cannot be empty"));

        verify(commentService, never()).createComment(eq(TASK_ID), eq(USER_ID), anyString());
    }

    // Тест: создание нового комментария с null контентом
    @Test
    void testCreateComment_NullContent() throws Exception {
        CommentRequestDto requestDto = new CommentRequestDto(null);

        mockMvc.perform(post("/api/comments")
                        .param("taskId", String.valueOf(TASK_ID))
                        .param("userId", String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed: content cannot be empty"));

        verify(commentService, never()).createComment(eq(TASK_ID), eq(USER_ID), anyString());
    }

    // Тест: Получение всех комментариев для задачи
    @Test
    void testGetCommentsByTaskId_Success() throws Exception {
        CommentDto commentDto = new CommentDto(COMMENT_ID, TASK_ID, USER_ID, USER_NAME, CONTENT, CREATED_AT);

        when(commentService.getCommentsByTaskId(TASK_ID)).thenReturn(Collections.singletonList(commentDto));

        mockMvc.perform(get("/api/comments/task/{taskId}", TASK_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(COMMENT_ID))
                .andExpect(jsonPath("$[0].content").value(CONTENT));

        verify(commentService, times(1)).getCommentsByTaskId(TASK_ID);
    }

    // Тест: Получение всех комментариев для несуществующей задачи
    @Test
    void testGetCommentsByTaskId_NonExistentTask() throws Exception {
        // Настройка мока для выбрасывания исключения
        when(commentService.getCommentsByTaskId(COMMENT_ID))
                .thenThrow(new TaskNotFoundException("Task not found"));

        // Выполнение запроса и проверка ответа
        mockMvc.perform(get("/api/comments/task/{taskId}", COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Ожидаем статус 404
                .andExpect(jsonPath("$.status").value(404)) // Проверяем код ошибки
                .andExpect(jsonPath("$.message").value("Task not found")); // Проверяем сообщение

        // Проверка, что метод сервиса был вызван один раз
        verify(commentService, times(1)).getCommentsByTaskId(COMMENT_ID);
    }

    // Тест: Обновление комментария
    @Test
    void testUpdateComment_Success() throws Exception {
        CommentRequestDto requestDto = new CommentRequestDto(UPDATED_CONTENT);
        CommentDto updatedDto = new CommentDto(COMMENT_ID, TASK_ID, USER_ID, USER_NAME, UPDATED_CONTENT, CREATED_AT);

        when(commentService.updateComment(eq(COMMENT_ID), eq(UPDATED_CONTENT))).thenReturn(updatedDto);

        mockMvc.perform(put("/api/comments/{commentId}", COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(COMMENT_ID))
                .andExpect(jsonPath("$.content").value(UPDATED_CONTENT));

        verify(commentService, times(1)).updateComment(eq(COMMENT_ID), eq(UPDATED_CONTENT));
    }

    // Тест: Попытка обновить несуществующий комментарий
    @Test
    void testUpdateComment_NonExistentComment() throws Exception {
        CommentRequestDto requestDto = new CommentRequestDto(UPDATED_CONTENT);

        when(commentService.updateComment(eq(COMMENT_ID), anyString()))
                .thenThrow(new CommentNotFoundException("Comment not found"));

        mockMvc.perform(put("/api/comments/{commentId}", COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Comment not found"));

        verify(commentService, times(1)).updateComment(eq(COMMENT_ID), anyString());
    }

    // Тест: Попытка обновить комментарий с пустым контентом
    @Test
    void testUpdateComment_EmptyContent() throws Exception {
        CommentRequestDto requestDto = new CommentRequestDto("");

        mockMvc.perform(put("/api/comments/{commentId}", COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed: content cannot be empty"));

        verify(commentService, never()).updateComment(eq(COMMENT_ID), anyString());
    }

    // Тест: Попытка обновить комментарий с пустым контентом
    @Test
    void testUpdateComment_NullContent() throws Exception {
        CommentRequestDto requestDto = new CommentRequestDto(null);

        mockMvc.perform(put("/api/comments/{commentId}", COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed: content cannot be empty"));

        verify(commentService, never()).updateComment(eq(COMMENT_ID), anyString());
    }

    // Тест: Удаление комментария
    @Test
    void testDeleteComment_Success() throws Exception {
        doNothing().when(commentService).deleteComment(COMMENT_ID);

        mockMvc.perform(delete("/api/comments/{commentId}", COMMENT_ID))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).deleteComment(COMMENT_ID);
    }

    // Тест: Попытка удалить несуществующий комментарий
    @Test
    void testDeleteComment_NonExistentComment() throws Exception {
        doThrow(new CommentNotFoundException("Comment not found")).when(commentService).deleteComment(COMMENT_ID);

        mockMvc.perform(delete("/api/comments/{commentId}", COMMENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Comment not found"));

        verify(commentService, times(1)).deleteComment(COMMENT_ID);
    }
}