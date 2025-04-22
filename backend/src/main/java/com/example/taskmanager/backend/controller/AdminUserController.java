package com.example.taskmanager.backend.controller;

import com.example.taskmanager.backend.dto.AuthRequestDto;
import com.example.taskmanager.backend.dto.AuthResponseDto;
import com.example.taskmanager.backend.dto.UserDto;
import com.example.taskmanager.backend.dto.UserRegistrationDto;
import com.example.taskmanager.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        UserDto createdUser = userService.register(registrationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/{userId}/block")
    public ResponseEntity<Void> blockUser(@PathVariable Long userId) {
        userService.blockUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/unblock")
    public ResponseEntity<Void> unblockUser(@PathVariable Long userId) {
        userService.unblockUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/make-admin")
    public ResponseEntity<Void> makeAdmin(@PathVariable Long userId) {
        userService.makeAdmin(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/make-user")
    public ResponseEntity<Void> makeUser(@PathVariable Long userId) {
        userService.makeUser(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

}
