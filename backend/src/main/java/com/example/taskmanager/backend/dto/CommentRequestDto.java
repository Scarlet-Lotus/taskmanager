package com.example.taskmanager.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentRequestDto {

    @NotBlank(message = "content cannot be empty")
    private String content;

    public CommentRequestDto() {}

    public CommentRequestDto(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
