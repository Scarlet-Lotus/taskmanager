package com.example.taskmanager.backend.security;

import com.example.taskmanager.backend.exception.ErrorResponse;
import com.example.taskmanager.backend.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtRequestFilter jwtRequestFilter;
    private final CustomAuthenticationProvider customAuthenticationProvider;
    private final ObjectMapper objectMapper;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtRequestFilter jwtRequestFilter, CustomAuthenticationProvider customAuthenticationProvider, ObjectMapper objectMapper) {
        this.customUserDetailsService = userDetailsService;
        this.jwtRequestFilter = jwtRequestFilter;
        this.customAuthenticationProvider = customAuthenticationProvider;
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Отключаем CSRF для API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Разрешаем доступ к /api/auth/**
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // Разрешить доступ к /api/admin/** только пользователям с ролью ADMIN
                        .anyRequest().authenticated() // Все остальные запросы требуют аутентификации
                )
                .authenticationProvider(customAuthenticationProvider) // Добавляем кастомный провайдер
                .exceptionHandling(ex -> ex
                        // Настройка AuthenticationEntryPoint для возврата JSON-ответа
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            ErrorResponse error = new ErrorResponse(
                                    LocalDateTime.now(),
                                    HttpStatus.UNAUTHORIZED.value(),
                                    authException.getMessage(),
                                    null
                            );
                            response.getWriter().write(objectMapper.writeValueAsString(error));
                        })
                        // Настройка AccessDeniedHandler для возврата JSON-ответа
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            ErrorResponse error = new ErrorResponse(
                                    LocalDateTime.now(),
                                    HttpStatus.FORBIDDEN.value(),
                                    accessDeniedException.getMessage(),
                                    null
                            );
                            response.getWriter().write(objectMapper.writeValueAsString(error));
                        })
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless-сессии
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class); // Добавляем JWT-фильтр

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
