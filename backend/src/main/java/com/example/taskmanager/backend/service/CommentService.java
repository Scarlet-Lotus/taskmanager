package com.example.taskmanager.backend.service;

import com.example.taskmanager.backend.dto.CommentDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(Long taskId, Long userId, String content);
    List<CommentDto> getCommentsByTaskId(Long taskId);
    CommentDto updateComment(Long commentId, String newContent);
    void deleteComment(Long commentId);

}
