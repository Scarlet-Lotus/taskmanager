package com.example.taskmanager.backend.service;

import com.example.taskmanager.backend.dto.TaskDto;
import com.example.taskmanager.backend.dto.TaskRequestDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskService {

    // Фильтрация по статусу
    List<TaskDto> filterByStatus(String status);

    // Фильтрация по приоритету
    List<TaskDto> filterByPriority(String priority);

    // Фильтрация по дедлайну
    @Query("SELECT t FROM Task t WHERE t.deadline <= :deadline")
    List<TaskDto> findByDeadlineBefore(@Param("deadline") LocalDate deadline);

    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE %:query% OR LOWER(t.description) LIKE %:query%")
    List<TaskDto> searchTasks(@Param("query") String query);

    void shareTask(Long taskId, Long userId);

    List<TaskDto> getAllTasks();

    TaskDto createTask(TaskRequestDto taskRequestDto);

    TaskDto updateTask(Long id, TaskRequestDto taskRequestDto);

    void deleteTask(Long id);

}
