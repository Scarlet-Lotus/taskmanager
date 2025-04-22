package com.example.taskmanager.backend.controller;

import com.example.taskmanager.backend.dto.TaskDto;
import com.example.taskmanager.backend.dto.TaskRequestDto;
import com.example.taskmanager.backend.service.TaskService;
import com.example.taskmanager.backend.validation.TaskValidation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/filter/status")
    public ResponseEntity<List<TaskDto>> filterByStatus(@RequestParam String status) {
        TaskValidation.validateStatus(status);
        return ResponseEntity.ok(taskService.filterByStatus(status));
    }

    @GetMapping("/filter/priority")
    public ResponseEntity<List<TaskDto>> filterByPriority(@RequestParam String priority) {
        TaskValidation.validatePriority(priority);
        return ResponseEntity.ok(taskService.filterByPriority(priority));
    }

    @GetMapping("/filter/deadline")
    public ResponseEntity<List<TaskDto>> filterByDeadline(@RequestParam LocalDate deadline) {
        return ResponseEntity.ok(taskService.findByDeadlineBefore(deadline));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TaskDto>> searchTasks(@RequestParam String query) {
        return ResponseEntity.ok(taskService.searchTasks(query));
    }

    @PostMapping("/{taskId}/share")
    public ResponseEntity<Void> shareTask(@PathVariable Long taskId, @RequestParam Long userId) {
        taskService.shareTask(taskId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<TaskDto>> getAllTasks() {
        List<TaskDto> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskRequestDto taskRequestDto) {
        TaskValidation.validateDto(taskRequestDto);
        TaskDto createdTask = taskService.createTask(taskRequestDto);
        return ResponseEntity.status(201).body(createdTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequestDto taskRequestDto) {
        TaskValidation.validateDto(taskRequestDto);
        TaskDto updatedTask = taskService.updateTask(id, taskRequestDto);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

}
