package com.example.taskmanager.backend.service;

import com.example.taskmanager.backend.dto.AuthRequestDto;
import com.example.taskmanager.backend.exception.InvalidCredentialsException;
import com.example.taskmanager.backend.exception.UserAlreadyExistsException;
import com.example.taskmanager.backend.model.User;
import com.example.taskmanager.backend.repository.UserRepository;
import com.example.taskmanager.backend.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String register(AuthRequestDto request) {
        log.info("Starting user registration with login: {}", request.getLogin());

        // Проверка на пустой login
        if (request.getLogin() == null || request.getLogin().trim().isEmpty()) {
            log.error("Login is required");
            throw new IllegalArgumentException("Login is required");
        }

        // Проверка на пустой email
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            log.error("Email is required");
            throw new IllegalArgumentException("Email is required");
        }

        // Проверка на пустой пароль
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            log.error("Password is required");
            throw new IllegalArgumentException("Password is required");
        }

        // Проверяем, существует ли пользователь с таким email
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("User with email {} already exists", request.getEmail());
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        // Проверяем, существует ли пользователь с таким login
        if (userRepository.existsByLogin(request.getLogin())) {
            log.warn("User with login {} already exists", request.getLogin());
            throw new UserAlreadyExistsException("User with this login already exists");
        }

        // Создаем нового пользователя
        User user = new User();
        user.setLogin(request.getLogin());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER"); // По умолчанию роль "USER"

        log.info("Saving new user with login: {}", request.getLogin());

        // Сохраняем пользователя в базе данных
        User savedUser = userRepository.save(user);

        log.info("User successfully registered with ID: {}", savedUser.getId());

        // Генерируем JWT-токен
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.isBlocked());
        log.info("Generated JWT token for user with email: {}", savedUser.getEmail());

        return token;
    }

    public String login(AuthRequestDto request) {
        log.info("Attempting login for user with email: {}", request.getEmail());

        // Проверяем, существует ли пользователь с таким email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("Invalid email: {}", request.getEmail());
                    return new InvalidCredentialsException("Invalid email");
                });

        // Проверяем, совпадает ли логин
        if (!user.getLogin().equals(request.getLogin())) {
            log.error("Invalid login for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid login");
        }

        // Проверяем, совпадает ли пароль
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("Invalid password for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid password");
        }

        log.info("Login successful for user with email: {}", request.getEmail());

        // Генерируем JWT-токен
        String token = jwtUtil.generateToken(user.getEmail(), user.isBlocked());
        log.info("Generated JWT token for user with email: {}", user.getEmail());

        return token;
    }
}
