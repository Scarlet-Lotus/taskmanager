package com.example.taskmanager.backend.controller;

import com.example.taskmanager.backend.dto.CommentDto;
import com.example.taskmanager.backend.dto.CommentRequestDto;
import com.example.taskmanager.backend.service.CommentService;
import com.example.taskmanager.backend.validation.CommentValidation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // Создать новый комментарий
    @PostMapping
    public ResponseEntity<CommentDto> createComment(
            @RequestParam Long taskId,
            @RequestParam Long userId,
            @Valid @RequestBody CommentRequestDto request) {
        CommentDto commentDto = commentService.createComment(taskId, userId, request.getContent());
        return ResponseEntity.status(201).body(commentDto);
    }

    // Получить все комментарии для задачи
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<CommentDto>> getCommentsByTaskId(@PathVariable Long taskId) {
        List<CommentDto> comments = commentService.getCommentsByTaskId(taskId);
        return ResponseEntity.ok(comments);
    }

    // Обновить комментарий
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDto request) {
        CommentDto updatedComment = commentService.updateComment(commentId, request.getContent());
        return ResponseEntity.ok(updatedComment);
    }

    // Удалить комментарий
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

}
