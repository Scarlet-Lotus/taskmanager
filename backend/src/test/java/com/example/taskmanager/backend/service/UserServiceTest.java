package com.example.taskmanager.backend.service;

import com.example.taskmanager.backend.dto.UserProfileDto;
import com.example.taskmanager.backend.dto.UserRegistrationDto;
import com.example.taskmanager.backend.dto.UserUpdateDto;
import com.example.taskmanager.backend.exception.UserAlreadyExistsException;
import com.example.taskmanager.backend.exception.UserNotFoundException;
import com.example.taskmanager.backend.model.Task;
import com.example.taskmanager.backend.model.TaskPriority;
import com.example.taskmanager.backend.model.TaskStatus;
import com.example.taskmanager.backend.model.User;
import com.example.taskmanager.backend.repository.TaskRepository;
import com.example.taskmanager.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_Success() {
        // Arrange
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "test",
                "test@example.com",
                "password"
        );

        User user = new User();
        user.setId(1L);
        user.setLogin(registrationDto.getLogin());
        user.setEmail(registrationDto.getEmail());
        user.setPassword("hashedPassword");
        user.setRole("USER");

        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(userRepository.existsByLogin(registrationDto.getLogin())).thenReturn(false);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        var result = userService.register(registrationDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_UserAlreadyExistsWithEmail() {
        // Arrange
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "new_user",
                "test@example.com",
                "password"
        );

        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(UserAlreadyExistsException.class, () -> userService.register(registrationDto));
        assertEquals("User with this email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_UserAlreadyExistsWithLogin() {
        // Arrange
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "test",
                "new_user@example.com",
                "password"
        );

        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(userRepository.existsByLogin(registrationDto.getLogin())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(UserAlreadyExistsException.class, () -> userService.register(registrationDto));
        assertEquals("User with this login already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_EmptyLogin() {
        // Arrange
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "",
                "test@example.com",
                "password"
        );

        // Act & Assert
        RuntimeException exception = assertThrows(IllegalArgumentException.class, () -> userService.register(registrationDto));
        assertEquals("Login is required", exception.getMessage());
    }

    @Test
    void testRegister_EmptyEmail() {
        // Arrange
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "test",
                "",
                "password"
        );

        // Act & Assert
        RuntimeException exception = assertThrows(IllegalArgumentException.class, () -> userService.register(registrationDto));
        assertEquals("Email is required", exception.getMessage());
    }

    @Test
    void testRegister_EmptyPassword() {
        // Arrange
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "test",
                "test@example.com",
                ""
        );

        // Act & Assert
        RuntimeException exception = assertThrows(IllegalArgumentException.class, () -> userService.register(registrationDto));
        assertEquals("Password is required", exception.getMessage());
    }

    @Test
    void testGetUserProfile_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");
        user.setRole("USER");

        Task task = new Task();
        task.setId(1L);
        task.setTitle("Task 1");
        task.setStatus(TaskStatus.TO_DO);
        task.setPriority(TaskPriority.HIGH);
        task.setUser(user);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.findByUserId(user.getId())).thenReturn(Collections.singletonList(task));

        // Act
        UserProfileDto profile = userService.getUserProfile("test@example.com");

        // Assert
        assertNotNull(profile);
        assertEquals("test@example.com", profile.getEmail());
        assertEquals(1, profile.getTasks().size());
        assertEquals("Task 1", profile.getTasks().get(0).getTitle());
    }

    @Test
    void testGetUserProfile_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(UserNotFoundException.class, () -> userService.getUserProfile("nonexistent@example.com"));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testUpdateUser_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");
        user.setFirstName("firstname");
        user.setLastName("lastname");
        user.setRole("USER");

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setLogin("newLogin");
        updateDto.setPassword("newPassword");
        updateDto.setFirstName("firstname");
        updateDto.setLastName("lastname");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByLogin("newLogin")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserProfileDto updatedProfile = userService.updateUser("test@example.com", updateDto);

        // Assert
        assertNotNull(updatedProfile);
        assertEquals("newLogin", updatedProfile.getLogin());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUser_LoginAlreadyExists() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");
        user.setFirstName("firstname");
        user.setLastName("lastname");
        user.setRole("USER");

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setLogin("existingLogin");
        updateDto.setPassword("hashedPassword");
        updateDto.setFirstName("firstname");
        updateDto.setLastName("lastname");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByLogin("existingLogin")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser("test@example.com", updateDto));
        assertEquals("User with this login already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUser_EmptyLogin() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password");

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setLogin(""); // Некорректный логин
        updateDto.setPassword("password");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act & Assert
        RuntimeException exception = assertThrows(IllegalArgumentException.class, () -> userService.updateUser("test@example.com", updateDto));
        assertEquals("Login cannot be empty", exception.getMessage());
    }

    @Test
    void testUpdateUser_EmptyPassword() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setLogin("test");
        user.setEmail("test@example.com");

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setLogin("test");
        updateDto.setPassword(""); // Некорректный пароль

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act & Assert
        RuntimeException exception = assertThrows(IllegalArgumentException.class, () -> userService.updateUser("test@example.com", updateDto));
        assertEquals("Password cannot be empty", exception.getMessage());
    }

    @Test
    void testDeleteUser_Success() {
        // Arrange: Настройка мока для существующего пользователя
        User user = new User();
        user.setId(1L);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act: Вызываем метод deleteUser
        userService.deleteUser(1L);

        // Assert: Проверяем, что метод deleteById был вызван
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteUser_UserNotFound() {
        // Arrange: Настройка мока для несуществующего пользователя
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert: Проверяем, что выбрасывается исключение
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(999L));

        // Verify: Проверяем, что метод deleteById не был вызван
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void testBlockUser_Success() {
        // Arrange: Настройка мока для существующего пользователя
        User user = new User();
        user.setId(1L);
        user.setBlocked(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act: Вызываем метод blockUser
        userService.blockUser(1L);

        // Assert: Проверяем, что пользователь заблокирован
        assertTrue(user.isBlocked());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUnblockUser_Success() {
        // Arrange: Настройка мока для существующего пользователя
        User user = new User();
        user.setId(1L);
        user.setBlocked(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act: Вызываем метод unblockUser
        userService.unblockUser(1L);

        // Assert: Проверяем, что пользователь разблокирован
        assertFalse(user.isBlocked());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testMakeAdmin_Success() {
        // Arrange: Настройка мока для существующего пользователя
        User user = new User();
        user.setId(1L);
        user.setRole("USER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act: Вызываем метод makeAdmin
        userService.makeAdmin(1L);

        // Assert: Проверяем, что роль пользователя изменена на "ADMIN"
        assertEquals("ADMIN", user.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testMakeUser_Success() {
        // Arrange: Настройка мока для существующего пользователя
        User user = new User();
        user.setId(1L);
        user.setRole("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act: Вызываем метод makeUser
        userService.makeUser(1L);

        // Assert: Проверяем, что роль пользователя изменена на "USER"
        assertEquals("USER", user.getRole());
        verify(userRepository, times(1)).save(user);
    }
}