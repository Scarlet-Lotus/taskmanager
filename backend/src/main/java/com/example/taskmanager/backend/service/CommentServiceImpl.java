package com.example.taskmanager.backend.service;

import com.example.taskmanager.backend.dto.CommentDto;
import com.example.taskmanager.backend.exception.CommentNotFoundException;
import com.example.taskmanager.backend.exception.TaskNotFoundException;
import com.example.taskmanager.backend.exception.UserNotFoundException;
import com.example.taskmanager.backend.mapper.CommentMapper;
import com.example.taskmanager.backend.model.Comment;
import com.example.taskmanager.backend.model.Task;
import com.example.taskmanager.backend.model.User;
import com.example.taskmanager.backend.repository.CommentRepository;
import com.example.taskmanager.backend.repository.TaskRepository;
import com.example.taskmanager.backend.repository.UserRepository;
import com.example.taskmanager.backend.validation.CommentValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public CommentServiceImpl(CommentRepository commentRepository, TaskRepository taskRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CommentDto createComment(Long taskId, Long userId, String content) {
        log.info("Creating a new comment for task ID: {} by user ID: {}", taskId, userId);

        // Находим задачу
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("Task not found with ID: {}", taskId);
                    return new TaskNotFoundException("Task not found");
                });
        log.debug("Found task with ID: {}", task.getId());

        // Находим пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found");
                });
        log.debug("Found user with ID: {}", user.getId());

        // Некорректные данные комментария
        CommentValidation.validateContent(content);
        log.debug("Content validation passed for comment");

        // Создаем новый комментарий
        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(user);
        comment.setContent(content);
        log.debug("Created new comment with content: {}", content);

        // Сохраняем комментарий
        Comment savedComment = commentRepository.save(comment);
        log.info("Comment successfully created with ID: {}", savedComment.getId());

        // Преобразуем в DTO
        CommentDto commentDto = CommentMapper.toDto(savedComment);
        log.debug("Mapped Comment to CommentDto: {}", commentDto);

        return commentDto;
    }

    @Override
    public List<CommentDto> getCommentsByTaskId(Long taskId) {
        log.info("Fetching comments for task ID: {}", taskId);

        if (!taskRepository.existsById(taskId)) {
            log.error("Task not found with ID: {}", taskId);
            throw new TaskNotFoundException("Task not found");
        }

        // Получаем комментарии из базы данных
        List<Comment> comments = commentRepository.findByTaskId(taskId);
        log.debug("Found {} comments for task ID: {}", comments.size(), taskId);

        // Преобразуем в DTO
        List<CommentDto> commentDtos = comments.stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
        log.info("Returning {} CommentDTOs for task ID: {}", commentDtos.size(), taskId);

        return commentDtos;
    }

    @Override
    public CommentDto updateComment(Long commentId, String newContent) {
        log.info("Updating comment with ID: {}", commentId);

        // Находим комментарий
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("Comment not found with ID: {}", commentId);
                    return new CommentNotFoundException("Comment not found");
                });
        log.debug("Found comment with ID: {}", comment.getId());

        // Некорректные данные комментария
        CommentValidation.validateContent(newContent);
        log.debug("Content validation passed for comment ID: {}", commentId);

        // Обновляем содержимое комментария
        comment.setContent(newContent);
        log.debug("Updated content for comment ID: {}", commentId);

        // Сохраняем обновленный комментарий
        Comment updatedComment = commentRepository.save(comment);
        log.info("Comment successfully updated with ID: {}", updatedComment.getId());

        // Преобразуем в DTO
        CommentDto commentDto = CommentMapper.toDto(updatedComment);
        log.debug("Mapped updated Comment to CommentDto: {}", commentDto);

        return commentDto;
    }

    @Override
    public void deleteComment(Long commentId) {
        log.info("Deleting comment with ID: {}", commentId);

        if (!commentRepository.existsById(commentId)) {
            log.error("Comment not found with ID: {}", commentId);
            throw new CommentNotFoundException("Comment not found");
        }

        log.debug("Deleting comment with ID: {}", commentId);
        commentRepository.deleteById(commentId);

        log.info("Comment successfully deleted with ID: {}", commentId);
    }

}
