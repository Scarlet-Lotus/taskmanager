package com.example.taskmanager.backend.controller;

import com.example.taskmanager.backend.dto.AuthRequestDto;
import com.example.taskmanager.backend.dto.AuthResponseDto;
import com.example.taskmanager.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody AuthRequestDto request) {
        String token = authService.register(request);
        if (token == null) {
            throw new RuntimeException("Authentication failed");
        }
        return ResponseEntity.ok(new AuthResponseDto(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto request) {
        String token = authService.login(request);
        if (token == null) {
            throw new RuntimeException("Authentication failed");
        }
        return ResponseEntity.ok(new AuthResponseDto(token));
    }

}
