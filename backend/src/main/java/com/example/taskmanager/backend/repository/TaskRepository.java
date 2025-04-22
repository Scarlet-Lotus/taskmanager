package com.example.taskmanager.backend.repository;

import com.example.taskmanager.backend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserId(Long userId);

    List<Task> findByStatus(String status);

    List<Task> findByPriority(String priority);

    @Query("SELECT t FROM Task t WHERE t.deadline <= :deadline")
    List<Task> findByDeadline(LocalDate deadline);

    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE %:query% OR LOWER(t.description) LIKE %:query%")
    List<Task> searchTasks(@Param("query") String query);

}
