package com.example.taskmanager.backend.dto;

import java.util.List;

public class UserProfileDto {

    private Long id;
    private String login;
    private String email;
    private String firstName = "";
    private String lastName = "";
    private String role;
    private List<TaskDto> tasks; // Список задач пользователя

    // Конструкторы
    public UserProfileDto() {}

    public UserProfileDto(Long id, String login, String email, String firstName, String lastName, String role, List<TaskDto> tasks) {
        this.id = id;
        this.login = login;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.tasks = tasks;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<TaskDto> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskDto> tasks) {
        this.tasks = tasks;
    }

}
