package com.example.taskmanager.backend.repository;

import com.example.taskmanager.backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Найти все комментарии для конкретной задачи
    List<Comment> findByTaskId(Long taskId);

    // Удалить комментарий по ID
    void deleteById(Long commentId);

    // Найти комментарий по ID
    Optional<Comment> findById(Long commentId);

}
