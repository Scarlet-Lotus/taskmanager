package com.example.taskmanager.backend.controller;

import com.example.taskmanager.backend.exception.CommentNotFoundException;
import com.example.taskmanager.backend.exception.GlobalExceptionHandler;
import com.example.taskmanager.backend.security.JwtRequestFilter;
import com.example.taskmanager.backend.security.JwtUtil;
import com.example.taskmanager.backend.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCommentController.class) // Тестируем только контроллер
@Import(GlobalExceptionHandler.class) // Импортируем глобальный обработчик
@AutoConfigureMockMvc(addFilters = false) // Отключаем фильтры безопасности
class AdminCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService; // Мокируем сервис

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    private static final Long EXISTING_COMMENT_ID = 1L;
    private static final Long NON_EXISTING_COMMENT_ID = 999L;

    @Test
    void testDeleteComment_Success() throws Exception {
        // Arrange: Настройка мока для успешного удаления комментария
        doNothing().when(commentService).deleteComment(EXISTING_COMMENT_ID);

        // Act & Assert: Выполняем запрос и проверяем ответ
        mockMvc.perform(delete("/api/admin/comments/{commentId}", EXISTING_COMMENT_ID))
                .andExpect(status().isNoContent()) // Ожидаем статус 204 No Content
                .andExpect(content().string("")); // Тело ответа должно быть пустым

        // Verify: Проверяем, что метод deleteComment был вызван с правильным ID
        verify(commentService, times(1)).deleteComment(EXISTING_COMMENT_ID);
    }

    @Test
    void testDeleteComment_NonExistentComment() throws Exception {
        // Arrange: Настройка мока для несуществующего комментария
        doThrow(new CommentNotFoundException("Comment not found"))
                .when(commentService).deleteComment(NON_EXISTING_COMMENT_ID);

        // Act & Assert: Выполняем запрос и проверяем ответ
        mockMvc.perform(delete("/api/admin/comments/{commentId}", NON_EXISTING_COMMENT_ID))
                .andExpect(status().isNotFound()) // Ожидаем статус 404 Not Found
                .andExpect(jsonPath("$.status").value(404)) // Проверяем JSON-ответ
                .andExpect(jsonPath("$.message").value("Comment not found"));

        // Verify: Проверяем, что метод deleteComment был вызван с правильным ID
        verify(commentService, times(1)).deleteComment(NON_EXISTING_COMMENT_ID);
    }

}