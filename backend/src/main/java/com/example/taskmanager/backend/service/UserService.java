package com.example.taskmanager.backend.service;

import com.example.taskmanager.backend.dto.UserDto;
import com.example.taskmanager.backend.dto.UserProfileDto;
import com.example.taskmanager.backend.dto.UserRegistrationDto;
import com.example.taskmanager.backend.dto.UserUpdateDto;

import java.util.List;

public interface UserService {

    UserDto register(UserRegistrationDto registrationDto);
    UserProfileDto getUserProfile(String email);
    UserProfileDto updateUser(String email, UserUpdateDto updateDto);
    List<UserDto> getAllUsers();
    void blockUser(Long userId);
    void unblockUser(Long userId);
    void makeAdmin(Long userId);
    void makeUser(Long userId);
    void deleteUser(Long userId);

}
