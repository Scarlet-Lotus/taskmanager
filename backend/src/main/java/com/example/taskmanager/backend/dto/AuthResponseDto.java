package com.example.taskmanager.backend.dto;

public class AuthResponseDto {

    private String token;

    public AuthResponseDto(String token) {
        this.token = token;
    }

    // Getter
    public String getToken() {
        return token;
    }

}
