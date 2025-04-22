package com.example.taskmanager.backend.repository;

import com.example.taskmanager.backend.model.SharedTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SharedTaskRepository extends JpaRepository<SharedTask, Long> {

    List<SharedTask> findBySharedWithUserId(Long userId);

}
