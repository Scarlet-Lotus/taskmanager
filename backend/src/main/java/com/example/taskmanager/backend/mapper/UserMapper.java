package com.example.taskmanager.backend.mapper;

import com.example.taskmanager.backend.dto.TaskDto;
import com.example.taskmanager.backend.dto.UserDto;
import com.example.taskmanager.backend.dto.UserProfileDto;
import com.example.taskmanager.backend.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public static UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getLogin(),
                user.getEmail(),
                user.getRole()
        );
    }

    public static UserProfileDto toProfileDto(User user, List<TaskDto> taskDtoList) {
        return new UserProfileDto(
                user.getId(),
                user.getLogin(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                taskDtoList
        );
    }

}
