package com.example.taskmanager.backend.controller;

import com.example.taskmanager.backend.dto.UserProfileDto;
import com.example.taskmanager.backend.dto.UserUpdateDto;
import com.example.taskmanager.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getUserProfile(email));
    }

    @PutMapping("/update")
    public ResponseEntity<UserProfileDto> updateUser(@Valid @RequestBody UserUpdateDto updateDto, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.updateUser(email, updateDto));
    }

}
