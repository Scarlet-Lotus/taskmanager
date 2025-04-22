package com.example.taskmanager.backend.mapper;

import com.example.taskmanager.backend.dto.TaskDto;
import com.example.taskmanager.backend.dto.TaskRequestDto;
import com.example.taskmanager.backend.exception.UserNotFoundException;
import com.example.taskmanager.backend.model.Task;
import com.example.taskmanager.backend.model.TaskPriority;
import com.example.taskmanager.backend.model.TaskStatus;
import com.example.taskmanager.backend.model.User;
import com.example.taskmanager.backend.repository.UserRepository;

import java.util.Optional;

public class TaskMapper {

    public static Task toEntity(TaskRequestDto requestDto, UserRepository userRepository) {
        Task task = new Task();
        task.setTitle(requestDto.getTitle());
        task.setDescription(requestDto.getDescription());
        task.setStatus(TaskStatus.valueOf(requestDto.getStatus()));
        task.setPriority(TaskPriority.valueOf(requestDto.getPriority()));
        task.setDeadline(requestDto.getDeadline());

        // Находим пользователя по userId и устанавливаем его в задачу
        if (requestDto.getUserId() == null) {
            throw new IllegalArgumentException("UserId cannot be empty");
        }
        Optional<User> userOptional = userRepository.findById(requestDto.getUserId());
        userOptional.ifPresent(task::setUser); // Устанавливаем пользователя, если он найден

        return task;
    }

    public static TaskDto toDto(Task task) {
        TaskDto taskDto = new TaskDto();
        taskDto.setId(task.getId());
        taskDto.setTitle(task.getTitle());
        taskDto.setDescription(task.getDescription());
        taskDto.setStatus(task.getStatus().name());
        taskDto.setPriority(task.getPriority().name());
        taskDto.setDeadline(task.getDeadline());
        taskDto.setUserId(task.getUser().getId());
        return taskDto;
    }

}
