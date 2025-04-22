package com.example.taskmanager.backend.service;

import com.example.taskmanager.backend.dto.TaskDto;
import com.example.taskmanager.backend.dto.TaskRequestDto;
import com.example.taskmanager.backend.exception.TaskNotFoundException;
import com.example.taskmanager.backend.exception.UserNotFoundException;
import com.example.taskmanager.backend.mapper.TaskMapper;
import com.example.taskmanager.backend.model.*;
import com.example.taskmanager.backend.repository.SharedTaskRepository;
import com.example.taskmanager.backend.repository.TaskRepository;
import com.example.taskmanager.backend.repository.UserRepository;
import com.example.taskmanager.backend.validation.TaskValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {


    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SharedTaskRepository sharedTaskRepository;

    public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository, SharedTaskRepository sharedTaskRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.sharedTaskRepository = sharedTaskRepository;
    }

    @Override
    public List<TaskDto> filterByStatus(String status) {
        log.info("Filtering tasks by status: {}", status);

        TaskValidation.validateStatus(status);
        log.debug("Status validation passed for: {}", status);

        List<Task> tasks = taskRepository.findByStatus(status);
        log.debug("Found {} tasks with status: {}", tasks.size(), status);

        List<TaskDto> taskDtos = tasks.stream()
                .map(TaskMapper::toDto)
                .collect(Collectors.toList());

        log.info("Returning {} tasks filtered by status: {}", taskDtos.size(), status);
        return taskDtos;
    }

    @Override
    public List<TaskDto> filterByPriority(String priority) {
        log.info("Filtering tasks by priority: {}", priority);

        TaskValidation.validatePriority(priority);
        log.debug("Priority validation passed for: {}", priority);

        List<Task> tasks = taskRepository.findByPriority(priority);
        log.debug("Found {} tasks with priority: {}", tasks.size(), priority);

        List<TaskDto> taskDtos = tasks.stream()
                .map(TaskMapper::toDto)
                .collect(Collectors.toList());

        log.info("Returning {} tasks filtered by priority: {}", taskDtos.size(), priority);
        return taskDtos;
    }

    @Override
    public List<TaskDto> findByDeadlineBefore(LocalDate deadline) {
        log.info("Filtering tasks by deadline before: {}", deadline);

        TaskValidation.validateDeadline(deadline);
        log.debug("Deadline validation passed for: {}", deadline);

        List<Task> tasks = taskRepository.findByDeadline(deadline);
        log.debug("Found {} tasks with deadline before: {}", tasks.size(), deadline);

        List<TaskDto> taskDtos = tasks.stream()
                .map(TaskMapper::toDto)
                .collect(Collectors.toList());

        log.info("Returning {} tasks filtered by deadline before: {}", taskDtos.size(), deadline);
        return taskDtos;
    }

    @Override
    public List<TaskDto> searchTasks(String query) {
        log.info("Searching tasks with query: {}", query);

        List<Task> tasks = taskRepository.searchTasks(query.toLowerCase());
        log.debug("Found {} tasks matching query: {}", tasks.size(), query);

        List<TaskDto> taskDtos = tasks.stream()
                .map(TaskMapper::toDto)
                .collect(Collectors.toList());

        log.info("Returning {} tasks matching query: {}", taskDtos.size(), query);
        return taskDtos;
    }

    @Override
    public void shareTask(Long taskId, Long userId) {
        log.info("Sharing task with ID: {} to user with ID: {}", taskId, userId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("Task not found with ID: {}", taskId);
                    return new TaskNotFoundException("Task not found");
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found");
                });

        log.debug("Creating shared task for task ID: {} and user ID: {}", taskId, userId);

        SharedTask sharedTask = new SharedTask();
        sharedTask.setTask(task);
        sharedTask.setSharedWithUser(user);
        sharedTaskRepository.save(sharedTask);

        log.info("Task with ID: {} successfully shared with user ID: {}", taskId, userId);
    }

    @Override
    public List<TaskDto> getAllTasks() {
        log.info("Fetching all tasks");

        List<Task> tasks = taskRepository.findAll();
        log.debug("Found {} tasks in the database", tasks.size());

        List<TaskDto> taskDtos = tasks.stream()
                .map(TaskMapper::toDto)
                .collect(Collectors.toList());

        log.info("Returning {} task DTOs", taskDtos.size());
        return taskDtos;
    }

    @Override
    public TaskDto createTask(TaskRequestDto taskRequestDto) {
        log.info("Creating a new task");

        TaskValidation.validateDto(taskRequestDto);
        log.debug("Task DTO validation passed");

        Task task = TaskMapper.toEntity(taskRequestDto, userRepository);
        log.debug("Mapped TaskRequestDto to Task entity");

        Task savedTask = taskRepository.save(task);
        log.info("Task successfully created with ID: {}", savedTask.getId());

        return TaskMapper.toDto(savedTask);
    }

    @Override
    public TaskDto updateTask(Long id, TaskRequestDto taskRequestDto) {
        log.info("Updating task with ID: {}", id);

        TaskValidation.validateDto(taskRequestDto);
        log.debug("Task DTO validation passed for task ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Task not found with ID: {}", id);
                    return new TaskNotFoundException("Task not found");
                });

        log.debug("Updating task data for task ID: {}", id);
        task.setTitle(taskRequestDto.getTitle());
        task.setDescription(taskRequestDto.getDescription());
        task.setStatus(TaskStatus.valueOf(taskRequestDto.getStatus()));
        task.setPriority(TaskPriority.valueOf(taskRequestDto.getPriority()));
        task.setDeadline(taskRequestDto.getDeadline());

        Task updatedTask = taskRepository.save(task);
        log.info("Task successfully updated with ID: {}", updatedTask.getId());

        return TaskMapper.toDto(updatedTask);
    }

    @Override
    public void deleteTask(Long id) {
        log.info("Deleting task with ID: {}", id);

        if (!taskRepository.existsById(id)) {
            log.error("Task not found with ID: {}", id);
            throw new TaskNotFoundException("Task not found");
        }

        log.debug("Deleting task with ID: {}", id);
        taskRepository.deleteById(id);

        log.info("Task successfully deleted with ID: {}", id);
    }

}
