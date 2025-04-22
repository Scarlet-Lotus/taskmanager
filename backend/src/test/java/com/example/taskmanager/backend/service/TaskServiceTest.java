package com.example.taskmanager.backend.service;

import com.example.taskmanager.backend.dto.TaskDto;
import com.example.taskmanager.backend.dto.TaskRequestDto;
import com.example.taskmanager.backend.mapper.TaskMapper;
import com.example.taskmanager.backend.model.*;
import com.example.taskmanager.backend.repository.SharedTaskRepository;
import com.example.taskmanager.backend.repository.TaskRepository;
import com.example.taskmanager.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SharedTaskRepository sharedTaskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFilterByStatus_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setFirstName("Firstname");
        user.setLastName("Lastname");
        user.setLogin("Login");
        user.setRole("USER");
        user.setEmail("email@example.com");
        user.setPassword("password");

        Task task = new Task();
        task.setId(1L);
        task.setTitle("Task 1");
        task.setDescription("Description 1");
        task.setStatus(TaskStatus.TO_DO);
        task.setPriority(TaskPriority.HIGH);
        task.setDeadline(LocalDate.now().plusDays(3));
        task.setUser(user);

        when(taskRepository.findByStatus("TO_DO")).thenReturn(List.of(task));

        // Act
        List<TaskDto> result = taskService.filterByStatus("TO_DO");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Task 1", result.get(0).getTitle());
        verify(taskRepository, times(1)).findByStatus("TO_DO");
    }

    @Test
    void testFilterByStatus_InvalidStatus() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> taskService.filterByStatus("INVALID_STATUS"));
        assertEquals("Invalid status: INVALID_STATUS", exception.getMessage());
        verify(taskRepository, never()).findByStatus(anyString());
    }

    @Test
    void testFilterByPriority_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setFirstName("Firstname");
        user.setLastName("Lastname");
        user.setLogin("Login");
        user.setRole("USER");
        user.setEmail("email@example.com");
        user.setPassword("password");

        Task task = new Task();
        task.setId(1L);
        task.setTitle("Task 1");
        task.setDescription("Description 1");
        task.setStatus(TaskStatus.TO_DO);
        task.setPriority(TaskPriority.HIGH);
        task.setDeadline(LocalDate.now().plusDays(3));
        task.setUser(user);

        when(taskRepository.findByPriority("HIGH")).thenReturn(List.of(task));

        // Act
        List<TaskDto> result = taskService.filterByPriority("HIGH");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Task 1", result.get(0).getTitle());
        verify(taskRepository, times(1)).findByPriority("HIGH");
    }

    @Test
    void testFilterByPriority_InvalidPriority() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> taskService.filterByPriority("INVALID_PRIORITY"));
        assertEquals("Invalid priority: INVALID_PRIORITY", exception.getMessage());
        verify(taskRepository, never()).findByPriority(anyString());
    }

    @Test
    void testSearchTasks_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setFirstName("Firstname");
        user.setLastName("Lastname");
        user.setLogin("Login");
        user.setRole("USER");
        user.setEmail("email@example.com");
        user.setPassword("password");

        Task task = new Task();
        task.setId(1L);
        task.setTitle("Complete Project");
        task.setDescription("Complete Project Done");
        task.setStatus(TaskStatus.TO_DO);
        task.setPriority(TaskPriority.HIGH);
        task.setDeadline(LocalDate.now().plusDays(3));
        task.setUser(user);

        when(taskRepository.searchTasks("project")).thenReturn(List.of(task));

        // Act
        List<TaskDto> result = taskService.searchTasks("project");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Complete Project", result.get(0).getTitle());
        verify(taskRepository, times(1)).searchTasks("project");
    }

    @Test
    void testSearchTasks_NoResults() {
        // Arrange
        String query = "nonexistent";
        when(taskRepository.searchTasks(query.toLowerCase())).thenReturn(Collections.emptyList());

        // Act
        List<TaskDto> result = taskService.searchTasks(query);

        // Assert
        assertTrue(result.isEmpty());
        verify(taskRepository, times(1)).searchTasks(query.toLowerCase());
    }

    @Test
    void testShareTask_Success() {
        // Arrange
        Long taskId = 1L;
        Long userId = 2L;

        Task task = new Task();
        task.setId(taskId);

        User user = new User();
        user.setId(userId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        taskService.shareTask(taskId, userId);

        // Assert
        verify(sharedTaskRepository, times(1)).save(any(SharedTask.class));
    }

    @Test
    void testShareTask_TaskNotFound() {
        // Arrange
        Long taskId = 999L;
        Long userId = 1L;

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> taskService.shareTask(taskId, userId));
        assertEquals("Task not found", exception.getMessage());
        verify(sharedTaskRepository, never()).save(any(SharedTask.class));
    }

    @Test
    void testShareTask_UserNotFound() {
        // Arrange
        Long taskId = 1L;
        Long userId = 999L;

        Task task = new Task();
        task.setId(taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> taskService.shareTask(taskId, userId));
        assertEquals("User not found", exception.getMessage());
        verify(sharedTaskRepository, never()).save(any(SharedTask.class));
    }

    @Test
    void testGetAllTasks_Success() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setFirstName("Firstname 1");
        user1.setLastName("Lastname 1");
        user1.setLogin("Login 1");
        user1.setRole("USER");
        user1.setEmail("email_1@example.com");
        user1.setPassword("password 1");

        User user2 = new User();
        user2.setId(2L);
        user2.setFirstName("Firstname 2");
        user2.setLastName("Lastname 2");
        user2.setLogin("Login 2");
        user2.setRole("USER");
        user2.setEmail("email_2@example.com");
        user2.setPassword("password 2");

        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");
        task1.setDescription("Description 1");
        task1.setStatus(TaskStatus.TO_DO);
        task1.setPriority(TaskPriority.HIGH);
        task1.setDeadline(LocalDate.now().plusDays(3));
        task1.setUser(user1);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setDescription("Description 2");
        task2.setStatus(TaskStatus.IN_PROGRESS);
        task2.setPriority(TaskPriority.MEDIUM);
        task2.setDeadline(LocalDate.now().plusDays(5));
        task2.setUser(user2);

        when(taskRepository.findAll()).thenReturn(List.of(task1, task2));

        // Act
        List<TaskDto> result = taskService.getAllTasks();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Task 1", result.get(0).getTitle());
        assertEquals("Task 2", result.get(1).getTitle());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void testCreateTask_Success() {
        // Arrange
        TaskRequestDto taskRequestDto = new TaskRequestDto(
                "New Task",
                "Description",
                "TO_DO",
                "HIGH",
                LocalDate.now().plusDays(3),
                1L
        );

        User user = new User();
        user.setId(1L);
        user.setLogin("test");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole("USER");

        when(userRepository.findById(taskRequestDto.getUserId())).thenReturn(Optional.of(user));

        Task task = TaskMapper.toEntity(taskRequestDto, userRepository);

        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        TaskDto result = taskService.createTask(taskRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals("New Task", result.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testCreateTask_InvalidData() {
        // Arrange
        TaskRequestDto taskRequestDto = new TaskRequestDto();
        taskRequestDto.setTitle(""); // Пустой заголовок
        taskRequestDto.setPriority("INVALID_PRIORITY");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> taskService.createTask(taskRequestDto));
        assertEquals("Task data cannot be null", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testUpdateTask_Success() {
        // Arrange
        Long taskId = 1L;

        User user = new User();
        user.setId(1L);
        user.setLogin("test");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole("USER");

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setTitle("Old Title");
        existingTask.setDescription("Old Description");
        existingTask.setStatus(TaskStatus.TO_DO);
        existingTask.setPriority(TaskPriority.HIGH);
        existingTask.setDeadline(LocalDate.now().plusDays(3));
        existingTask.setUser(user);

        TaskRequestDto updatedTaskRequestDto = new TaskRequestDto();
        updatedTaskRequestDto.setTitle("Updated Title");
        updatedTaskRequestDto.setDescription("Updated Description");
        updatedTaskRequestDto.setStatus("IN_PROGRESS");
        updatedTaskRequestDto.setPriority("MEDIUM");
        updatedTaskRequestDto.setDeadline(LocalDate.now().plusDays(2));
        updatedTaskRequestDto.setUserId(1L);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        // Act
        TaskDto result = taskService.updateTask(taskId, updatedTaskRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testUpdateTask_TaskNotFound() {
        // Arrange
        Long taskId = 999L;
        TaskRequestDto taskRequestDto = new TaskRequestDto();
        taskRequestDto.setTitle("Updated Title");
        taskRequestDto.setDescription("Updated Description");
        taskRequestDto.setStatus("IN_PROGRESS");
        taskRequestDto.setPriority("LOW");
        taskRequestDto.setDeadline(LocalDate.now().plusDays(1));
        taskRequestDto.setUserId(999L);

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> taskService.updateTask(taskId, taskRequestDto));
        assertEquals("Task not found", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testUpdateTask_InvalidData() {
        // Arrange
        Long taskId = 1L;
        TaskRequestDto taskRequestDto = new TaskRequestDto();
        taskRequestDto.setTitle(""); // Пустой заголовок

        Task task = new Task();
        task.setId(taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> taskService.updateTask(taskId, taskRequestDto));
        assertEquals("Task data cannot be null", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testDeleteTask_Success() {
        // Arrange
        Long taskId = 1L;

        when(taskRepository.existsById(taskId)).thenReturn(true);

        // Act
        taskService.deleteTask(taskId);

        // Assert
        verify(taskRepository, times(1)).deleteById(taskId);
    }

    @Test
    void testDeleteTask_TaskNotFound() {
        // Arrange
        Long taskId = 999L;

        when(taskRepository.existsById(taskId)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> taskService.deleteTask(taskId));
        assertEquals("Task not found", exception.getMessage());
        verify(taskRepository, never()).deleteById(taskId);
    }

}