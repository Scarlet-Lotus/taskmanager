package com.example.taskmanager.backend.validation;

import com.example.taskmanager.backend.dto.TaskRequestDto;
import com.example.taskmanager.backend.model.TaskPriority;
import com.example.taskmanager.backend.model.TaskStatus;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class TaskValidation {

    public static boolean isValidStatus(String status) {
        try {
            TaskStatus.valueOf(status); // Проверка, что статус существует в перечислении
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidPriority(String priority) {
        try {
            TaskPriority.valueOf(priority); // Проверка, что приоритет существует в перечислении
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidDeadline(String deadline) {
        LocalDate parsedDeadline;
        try {
            parsedDeadline = LocalDate.parse(deadline); // Попытка преобразовать строку в LocalDate
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isValidDeadline(LocalDate deadline) {
        if (deadline == null) {
            return false;
        }

        LocalDate today = LocalDate.now();

        return !deadline.isBefore(today);
    }

    public static void validateStatus(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (!TaskValidation.isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    public static void validatePriority(String priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }
        if (!TaskValidation.isValidPriority(priority)) {
            throw new IllegalArgumentException("Invalid priority: " + priority);
        }
    }

    public static void validateDeadline(LocalDate deadline) {
        if (deadline == null) {
            throw new IllegalArgumentException("Deadline cannot be null");
        }
    }

    public static void validateDto(TaskRequestDto requestDto) {
        if (requestDto.getTitle() == null || requestDto.getTitle().trim().isEmpty() ||
                requestDto.getDescription() == null || requestDto.getDescription().trim().isEmpty() ||
                requestDto.getStatus() == null || requestDto.getStatus().trim().isEmpty() ||
                requestDto.getPriority() == null || requestDto.getPriority().trim().isEmpty() ||
                requestDto.getDeadline() == null ||
                requestDto.getUserId() == null) {
            throw new IllegalArgumentException("Task data cannot be null");
        }
        if (!TaskValidation.isValidStatus(requestDto.getStatus())) {
            throw new IllegalArgumentException("Invalid task data: status is not valid");
        }
        if (!TaskValidation.isValidPriority(requestDto.getPriority())) {
            throw new IllegalArgumentException("Invalid task data: priority is not valid");
        }
        if (!TaskValidation.isValidDeadline(requestDto.getDeadline())) {
            throw new IllegalArgumentException("Invalid task data: deadline is not valid");
        }
    }

}
