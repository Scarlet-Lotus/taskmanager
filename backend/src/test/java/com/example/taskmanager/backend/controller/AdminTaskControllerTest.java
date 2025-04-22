package com.example.taskmanager.backend.controller;

import com.example.taskmanager.backend.exception.GlobalExceptionHandler;
import com.example.taskmanager.backend.exception.TaskNotFoundException;
import com.example.taskmanager.backend.security.JwtRequestFilter;
import com.example.taskmanager.backend.security.JwtUtil;
import com.example.taskmanager.backend.service.TaskService;
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

@WebMvcTest(AdminTaskController.class) // Тестируем только контроллер
@Import(GlobalExceptionHandler.class) // Импортируем глобальный обработчик
@AutoConfigureMockMvc(addFilters = false) // Отключаем фильтры безопасности
class AdminTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService; // Мокируем сервис

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    private static final Long EXISTING_TASK_ID = 1L;
    private static final Long NON_EXISTING_TASK_ID = 999L;

    @Test
    void testDeleteTask_Success() throws Exception {
        // Arrange: Настройка мока для успешного удаления задачи
        doNothing().when(taskService).deleteTask(EXISTING_TASK_ID);

        // Act & Assert: Выполняем запрос и проверяем ответ
        mockMvc.perform(delete("/api/admin/tasks/{taskId}", EXISTING_TASK_ID))
                .andExpect(status().isNoContent()) // Ожидаем статус 204 No Content
                .andExpect(content().string("")); // Тело ответа должно быть пустым

        // Verify: Проверяем, что метод deleteTask был вызван с правильным ID
        verify(taskService, times(1)).deleteTask(EXISTING_TASK_ID);
    }

    @Test
    void testDeleteTask_NonExistentTask() throws Exception {
        // Arrange: Настройка мока для несуществующей задачи
        doThrow(new TaskNotFoundException("Task not found"))
                .when(taskService).deleteTask(NON_EXISTING_TASK_ID);

        // Act & Assert: Выполняем запрос и проверяем ответ
        mockMvc.perform(delete("/api/admin/tasks/{taskId}", NON_EXISTING_TASK_ID))
                .andExpect(status().isNotFound()) // Ожидаем статус 404 Not Found
                .andExpect(jsonPath("$.status").value(404)) // Проверяем JSON-ответ
                .andExpect(jsonPath("$.message").value("Task not found"));

        // Verify: Проверяем, что метод deleteTask был вызван с правильным ID
        verify(taskService, times(1)).deleteTask(NON_EXISTING_TASK_ID);
    }

}