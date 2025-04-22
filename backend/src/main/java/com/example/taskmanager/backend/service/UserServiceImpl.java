package com.example.taskmanager.backend.service;

import com.example.taskmanager.backend.dto.*;
import com.example.taskmanager.backend.exception.UserAlreadyExistsException;
import com.example.taskmanager.backend.exception.UserNotFoundException;
import com.example.taskmanager.backend.mapper.TaskMapper;
import com.example.taskmanager.backend.mapper.UserMapper;
import com.example.taskmanager.backend.model.Task;
import com.example.taskmanager.backend.model.User;
import com.example.taskmanager.backend.repository.TaskRepository;
import com.example.taskmanager.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, TaskRepository taskRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto register(UserRegistrationDto registrationDto) {
        log.info("Starting user registration with login: {}", registrationDto.getLogin());

        // Если login пустой, userRepository.save() не вызывается, и user остается null
        if (registrationDto.getLogin() == null || registrationDto.getLogin().trim().isEmpty()) {
            log.error("Login is required");
            throw new IllegalArgumentException("Login is required");
        }

        // Если email пустой, userRepository.save() не вызывается, и user остается null
        if (registrationDto.getEmail() == null || registrationDto.getEmail().trim().isEmpty()) {
            log.error("Email is required");
            throw new IllegalArgumentException("Email is required");
        }

        // Если password пустой, userRepository.save() не вызывается, и user остается null
        if (registrationDto.getPassword() == null || registrationDto.getPassword().trim().isEmpty()) {
            log.error("Password is required");
            throw new IllegalArgumentException("Password is required");
        }

        // Проверяем, существует ли пользователь с таким email
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            log.warn("User with email {} already exists", registrationDto.getEmail());
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        // Проверяем, существует ли пользователь с таким login
        if (userRepository.existsByLogin(registrationDto.getLogin())) {
            log.warn("User with login {} already exists", registrationDto.getLogin());
            throw new UserAlreadyExistsException("User with this login already exists");
        }

        // Создаем нового пользователя
        User user = new User();
        user.setLogin(registrationDto.getLogin());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setRole("USER"); // По умолчанию роль "USER"

        // Сохраняем пользователя в базе данных
        log.info("Saving new user with login: {}", registrationDto.getLogin());
        User savedUser = userRepository.save(user);

        // Возвращаем DTO
        log.info("User successfully registered with ID: {}", savedUser.getId());
        return UserMapper.toDto(savedUser);
    }

    @Override
    public UserProfileDto getUserProfile(String email) {
        log.info("Fetching profile for user with email: {}", email);

        // Находим пользователя по email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UserNotFoundException("User not found");
                });

        log.debug("Found user with ID: {}", user.getId());

        // Получаем задачи пользователя
        List<Task> tasks = taskRepository.findByUserId(user.getId());
        log.debug("Found {} tasks for user with ID: {}", tasks.size(), user.getId());

        // Преобразуем задачи в DTO
        List<TaskDto> taskDtoList = tasks.stream()
                .map(TaskMapper::toDto)
                .collect(Collectors.toList());

        log.info("Returning profile for user with email: {}", email);
        return UserMapper.toProfileDto(user, taskDtoList);
    }

    @Override
    public UserProfileDto updateUser(String email, UserUpdateDto updateDto) {
        log.info("Updating user with email: {}", email);

        // Находим пользователя по email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UserNotFoundException("User not found");
                });

        log.debug("Found user with ID: {}", user.getId());

        // Проверяем корректность данных
        if (updateDto.getLogin() == null || updateDto.getLogin().trim().isEmpty()) {
            log.error("Login is empty for user with email: {}", email);
            throw new IllegalArgumentException("Login cannot be empty");
        }
        if (updateDto.getPassword() == null || updateDto.getPassword().trim().isEmpty()) {
            log.error("Password is empty for user with email: {}", email);
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (updateDto.getFirstName() == null || updateDto.getFirstName().trim().isEmpty()) {
            log.error("Firstname is empty for user with email: {}", email);
            throw new IllegalArgumentException("Firstname cannot be empty");
        }
        if (updateDto.getLastName() == null || updateDto.getLastName().trim().isEmpty()) {
            log.error("Lastname is empty for user with email: {}", email);
            throw new IllegalArgumentException("Lastname cannot be empty");
        }

        // Проверяем существование логина
        if (userRepository.existsByLogin(updateDto.getLogin())) {
            log.warn("User with login {} already exists", updateDto.getLogin());
            throw new UserAlreadyExistsException("User with this login already exists");
        }

        // Обновляем данные пользователя
        log.debug("Updating user data for user with email: {}", email);
        user.setLogin(updateDto.getLogin());
        user.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        user.setFirstName(updateDto.getFirstName());
        user.setLastName(updateDto.getLastName());

        // Сохраняем обновленного пользователя
        User updatedUser = userRepository.save(user);
        log.info("User successfully updated with ID: {}", updatedUser.getId());

        // Получаем задачи пользователя
        List<Task> tasks = taskRepository.findByUserId(updatedUser.getId());
        log.debug("Found {} tasks for user with ID: {}", tasks.size(), updatedUser.getId());

        // Преобразуем задачи в DTO
        List<TaskDto> taskDtoList = tasks.stream()
                .map(TaskMapper::toDto)
                .collect(Collectors.toList());

        log.info("Returning updated profile for user with email: {}", email);
        return UserMapper.toProfileDto(updatedUser, taskDtoList);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Fetching all users");

        List<User> users = userRepository.findAll();
        log.debug("Found {} users in the database", users.size());

        List<UserDto> userDtos = users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());

        log.info("Returning {} user DTOs", userDtos.size());
        return userDtos;
    }

    @Override
    public void blockUser(Long userId) {
        log.info("Blocking user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found");
                });

        log.debug("Blocking user with ID: {}", user.getId());
        user.setBlocked(true);
        userRepository.save(user);

        log.info("User successfully blocked with ID: {}", user.getId());
    }

    @Override
    public void unblockUser(Long userId) {
        log.info("Unblocking user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found");
                });

        log.debug("Unblocking user with ID: {}", user.getId());
        user.setBlocked(false);
        userRepository.save(user);

        log.info("User successfully unblocked with ID: {}", user.getId());
    }

    @Override
    public void makeAdmin(Long userId) {
        log.info("Granting admin role to user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found");
                });

        log.debug("Setting role 'ADMIN' for user with ID: {}", user.getId());
        user.setRole("ADMIN");
        userRepository.save(user);

        log.info("User successfully granted admin role with ID: {}", user.getId());
    }

    @Override
    public void makeUser(Long userId) {
        log.info("Revoking admin role from user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found");
                });

        log.debug("Setting role 'USER' for user with ID: {}", user.getId());
        user.setRole("USER");
        userRepository.save(user);

        log.info("User successfully set to regular role with ID: {}", user.getId());
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw new UserNotFoundException("User not found");
        }

        log.debug("Deleting user with ID: {}", userId);
        userRepository.deleteById(userId);

        log.info("User successfully deleted with ID: {}", userId);
    }

}
