package com.example.taskmanager.backend.validation;

import com.example.taskmanager.backend.dto.CommentDto;

public class CommentValidation {

    public static void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }
    }

    public static void  validateDto(CommentDto dto) {
        validateContent(dto.getContent());
        if (dto.getId() == null ||
                dto.getTaskId() == null ||
                dto.getUserId() == null ||
                dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment data cannot be empty");
        }
    }

}
