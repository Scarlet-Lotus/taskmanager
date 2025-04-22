package com.example.taskmanager.backend.mapper;

import com.example.taskmanager.backend.dto.CommentDto;
import com.example.taskmanager.backend.model.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public static CommentDto toDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getTask().getId(),
                comment.getUser().getId(),
                comment.getUser().getLogin(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }

}
