package com.example.taskmanager.backend.controller;

import com.example.taskmanager.backend.dto.TaskDto;
import com.example.taskmanager.backend.dto.TaskRequestDto;
import com.example.taskmanager.backend.exception.GlobalExceptionHandler;
import com.example.taskmanager.backend.exception.TaskNotFoundException;
import com.example.taskmanager.backend.exception.UserNotFoundException;
import com.example.taskmanager.backend.repository.TaskRepository;
import com.example.taskmanager.backend.security.JwtRequestFilter;
import com.example.taskmanager.backend.security.JwtUtil;
import com.example.taskmanager.backend.service.TaskService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import(GlobalExceptionHandler.class) // Импортируем глобальный обработчик
@AutoConfigureMockMvc(addFilters = false) // Отключаем фильтры безопасности
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private TaskRepository taskRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long TASK_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final String STATUS = "TO_DO";
    private static final String PRIORITY = "HIGH";
    private static final LocalDate DEADLINE = LocalDate.now().plusDays(3);
    private static final String SEARCH_QUERY = "urgent";

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    private TaskDto createSampleTaskDto() {
        return new TaskDto(
                TASK_ID,
                "Title",
                "Description",
                STATUS,
                PRIORITY,
                DEADLINE,
                USER_ID
        );
    }

    @Test
    void testFilterByStatus_Success() throws Exception {
        List<TaskDto> tasks = List.of(createSampleTaskDto());
        when(taskService.filterByStatus(STATUS)).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks/filter/status")
                        .param("status", STATUS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Title"));

        verify(taskService, times(1)).filterByStatus(STATUS);
    }

    @Test
    void testFilterByStatus_InvalidStatus() throws Exception {
        mockMvc.perform(get("/api/tasks/filter/status")
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid status: INVALID_STATUS"));
    }

    @Test
    void testFilterByPriority_Success() throws Exception {
        List<TaskDto> tasks = List.of(createSampleTaskDto());
        when(taskService.filterByPriority(PRIORITY)).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks/filter/priority")
                        .param("priority", PRIORITY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority").value(PRIORITY));

        verify(taskService, times(1)).filterByPriority(PRIORITY);
    }

    @Test
    void testFilterByPriority_InvalidPriority() throws Exception {
        mockMvc.perform(get("/api/tasks/filter/priority")
                        .param("priority", "INVALID_PRIORITY"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid priority: INVALID_PRIORITY"));
    }

    @Test
    void testFilterByDeadline_Success() throws Exception {
        List<TaskDto> tasks = List.of(createSampleTaskDto());
        when(taskService.findByDeadlineBefore(DEADLINE)).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks/filter/deadline")
                        .param("deadline", DEADLINE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deadline").value(DEADLINE.toString()));

        verify(taskService, times(1)).findByDeadlineBefore(DEADLINE);
    }

//    @Test
//    void testFilterByDeadline_InvalidDeadline() throws Exception {
//        mockMvc.perform(get("/api/tasks/filter/deadline")
//                        .param("deadline", (String) null))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.status").value(400))
//                .andExpect(jsonPath("$.message").value("Invalid deadline"));
//    }

    @Test
    void testSearchTasks_Success() throws Exception {
        List<TaskDto> tasks = List.of(createSampleTaskDto());
        when(taskService.searchTasks(SEARCH_QUERY)).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks/search")
                        .param("query", SEARCH_QUERY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Title"));

        verify(taskService, times(1)).searchTasks(SEARCH_QUERY);
    }

    @Test
    void testShareTask_Success() throws Exception {
        doNothing().when(taskService).shareTask(TASK_ID, USER_ID);

        mockMvc.perform(post("/api/tasks/{taskId}/share", TASK_ID)
                        .param("userId", String.valueOf(USER_ID)))
                .andExpect(status().isOk());

        verify(taskService, times(1)).shareTask(TASK_ID, USER_ID);
    }

    @Test
    void testShareTask_NonExistentTask() throws Exception {
        Long nonExistentTaskId = 999L;
        Long userId = 2L;

        doThrow(new TaskNotFoundException("Task not found"))
                .when(taskService).shareTask(nonExistentTaskId, userId);

        mockMvc.perform(post("/api/tasks/{taskId}/share", nonExistentTaskId)
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found"));
    }

    @Test
    void testShareTask_NonExistentUser() throws Exception {
        Long taskId = 1L;
        Long nonExistentUserId = 999L;

        doThrow(new UserNotFoundException("User not found"))
                .when(taskService).shareTask(taskId, nonExistentUserId);

        mockMvc.perform(post("/api/tasks/{taskId}/share", taskId)
                        .param("userId", String.valueOf(nonExistentUserId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void testGetAllTasks_Success() throws Exception {
        List<TaskDto> tasks = List.of(createSampleTaskDto());
        when(taskService.getAllTasks()).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(TASK_ID));

        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    void testCreateTask_Success() throws Exception {
        TaskDto taskDto = createSampleTaskDto();
        when(taskService.createTask(any(TaskRequestDto.class))).thenReturn(taskDto);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Title"));

        verify(taskService, times(1)).createTask(any(TaskRequestDto.class));
    }

    @Test
    void testCreateTask_InvalidData() throws Exception {
        TaskRequestDto invalidTaskRequestDto = new TaskRequestDto();
        invalidTaskRequestDto.setTitle(""); // Некорректное значение

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTaskRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testUpdateTask_Success() throws Exception {
        TaskDto updatedTaskDto = createSampleTaskDto();
        when(taskService.updateTask(eq(TASK_ID), any(TaskRequestDto.class))).thenReturn(updatedTaskDto);

        mockMvc.perform(put("/api/tasks/{id}", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTaskDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Title"));

        verify(taskService, times(1)).updateTask(eq(TASK_ID), any(TaskRequestDto.class));
    }

    @Test
    void testUpdateTask_NonExistentTask() throws Exception {
        Long nonExistentTaskId = 999L;
        TaskDto taskDto = createSampleTaskDto();

        when(taskService.updateTask(eq(nonExistentTaskId), any(TaskRequestDto.class)))
                .thenThrow(new TaskNotFoundException("Task not found"));

        mockMvc.perform(put("/api/tasks/{id}", nonExistentTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found"));
    }

    @Test
    void testUpdateTask_InvalidData() throws Exception {
        Long taskId = 1L;
        TaskRequestDto invalidTaskRequestDto = new TaskRequestDto();
        invalidTaskRequestDto.setTitle(""); // Некорректное значение

        when(taskService.updateTask(taskId, invalidTaskRequestDto))
                .thenThrow(new IllegalArgumentException("Invalid task data"));

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTaskRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed: deadline cannot be null, description cannot be empty, priority cannot be empty, status cannot be empty, title cannot be empty, userId cannot be null"));
    }

    @Test
    void testDeleteTask_Success() throws Exception {
        doNothing().when(taskService).deleteTask(TASK_ID);

        mockMvc.perform(delete("/api/tasks/{id}", TASK_ID))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask(TASK_ID);
    }

    @Test
    void testDeleteTask_NonExistentTask() throws Exception {
        Long nonExistentTaskId = 999L;

        doThrow(new TaskNotFoundException("Task not found"))
                .when(taskService).deleteTask(nonExistentTaskId);

        mockMvc.perform(delete("/api/tasks/{id}", nonExistentTaskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found"));
    }

}