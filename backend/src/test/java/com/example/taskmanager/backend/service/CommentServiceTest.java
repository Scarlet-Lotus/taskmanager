package com.example.taskmanager.backend.service;

import com.example.taskmanager.backend.dto.CommentDto;
import com.example.taskmanager.backend.model.Comment;
import com.example.taskmanager.backend.model.Task;
import com.example.taskmanager.backend.model.User;
import com.example.taskmanager.backend.repository.CommentRepository;
import com.example.taskmanager.backend.repository.TaskRepository;
import com.example.taskmanager.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateComment_Success() {
        // Arrange
        Long taskId = 1L;
        Long userId = 1L;
        String content = "This is a comment";

        Task task = new Task();
        task.setId(taskId);

        User user = new User();
        user.setId(userId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CommentDto result = commentService.createComment(taskId, userId, content);

        // Assert
        assertNotNull(result);
        assertEquals(content, result.getContent());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testCreateComment_TaskNotFound() {
        // Arrange
        Long taskId = 999L;
        Long userId = 1L;
        String content = "This is a comment";

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> commentService.createComment(taskId, userId, content));
        assertEquals("Task not found", exception.getMessage());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void testCreateComment_UserNotFound() {
        // Arrange
        Long taskId = 1L;
        Long userId = 999L;
        String content = "This is a comment";

        Task task = new Task();
        task.setId(taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> commentService.createComment(taskId, userId, content));
        assertEquals("User not found", exception.getMessage());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void testCreateComment_InvalidData() {
        // Arrange
        Long taskId = 1L;
        Long userId = 1L;
        String content = ""; // Пустой комментарий

        Task task = new Task();
        task.setId(taskId);

        User user = new User();
        user.setId(userId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> commentService.createComment(taskId, userId, content));
        assertEquals("Content cannot be empty", exception.getMessage());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void testGetCommentsByTaskId_Success() {
        // Arrange
        Long taskId = 1L;

        Task task = new Task();
        task.setId(taskId);
        task.setTitle("This is a task");

        User user = new User();
        user.setId(1L);
        user.setLogin("test");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole("USER");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setTask(task);
        comment.setUser(user);
        comment.setContent("This is a comment");

        when(taskRepository.existsById(taskId)).thenReturn(true);
        when(commentRepository.findByTaskId(taskId)).thenReturn(List.of(comment));

        // Act
        List<CommentDto> result = commentService.getCommentsByTaskId(taskId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("This is a comment", result.get(0).getContent());
        verify(taskRepository, times(1)).existsById(taskId);
        verify(commentRepository, times(1)).findByTaskId(taskId);
    }

    @Test
    void testGetCommentsByTaskId_NoComments() {
        // Arrange
        Long taskId = 1L;

        when(taskRepository.existsById(taskId)).thenReturn(true);
        when(commentRepository.findByTaskId(taskId)).thenReturn(List.of());

        // Act
        List<CommentDto> result = commentService.getCommentsByTaskId(taskId);

        // Assert
        assertTrue(result.isEmpty());
        verify(taskRepository, times(1)).existsById(taskId);
        verify(commentRepository, times(1)).findByTaskId(taskId);
    }

    @Test
    void testUpdateComment_Success() {
        // Arrange
        Long commentId = 1L;
        String newContent = "Updated comment";

        Task task = new Task();
        task.setId(1L);
        task.setTitle("this is a task");

        User user = new User();
        user.setId(1L);
        user.setLogin("test");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole("USER");

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setContent("Old comment");
        comment.setTask(task);
        comment.setUser(user);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CommentDto result = commentService.updateComment(commentId, newContent);

        // Assert
        assertNotNull(result);
        assertEquals(newContent, result.getContent());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testUpdateComment_CommentNotFound() {
        // Arrange
        Long commentId = 999L;
        String newContent = "Updated comment";

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> commentService.updateComment(commentId, newContent));
        assertEquals("Comment not found", exception.getMessage());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void testUpdateComment_InvalidData() {
        // Arrange
        Long commentId = 1L;
        String newContent = ""; // Пустой комментарий

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setContent("Old comment");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> commentService.updateComment(commentId, newContent));
        assertEquals("Content cannot be empty", exception.getMessage());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void testDeleteComment_Success() {
        // Arrange
        Long commentId = 1L;

        when(commentRepository.existsById(commentId)).thenReturn(true);

        // Act
        commentService.deleteComment(commentId);

        // Assert
        verify(commentRepository, times(1)).deleteById(commentId);
    }

    @Test
    void testDeleteComment_CommentNotFound() {
        // Arrange
        Long commentId = 999L;

        when(commentRepository.existsById(commentId)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> commentService.deleteComment(commentId));
        assertEquals("Comment not found", exception.getMessage());
        verify(commentRepository, never()).deleteById(commentId);
    }

}