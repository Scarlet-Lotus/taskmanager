package com.example.taskmanager.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName = "";

    private String lastName = "";

    @Column(nullable = false)
    private String role; // USER or ADMIN

    @Column(nullable = false)
    private boolean isBlocked = false; // Флаг блокировки

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Преобразуем роль пользователя в формат GrantedAuthority
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return email; // Используем email как логин
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Учетная запись не истекает
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isBlocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Учетные данные не истекли
    }

    @Override
    public boolean isEnabled() {
        return true; // Учетная запись активна
    }

}