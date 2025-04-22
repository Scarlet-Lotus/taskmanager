package com.example.taskmanager.backend.service;

import com.example.taskmanager.backend.dto.AuthRequestDto;
import com.example.taskmanager.backend.exception.InvalidCredentialsException;
import com.example.taskmanager.backend.exception.UserAlreadyExistsException;
import com.example.taskmanager.backend.model.User;
import com.example.taskmanager.backend.repository.UserRepository;
import com.example.taskmanager.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_Success() {
        // Arrange
        AuthRequestDto request = new AuthRequestDto();
        request.setLogin("testUser");
        request.setEmail("test@example.com");
        request.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setLogin(request.getLogin());
        user.setEmail(request.getEmail());
        user.setPassword("hashed password");
        user.setRole("USER");

        // Настройка моков
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByLogin(request.getLogin())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(request.getEmail(), false)).thenReturn("generated-jwt-token");

        // Act
        String token = authService.register(request);

        // Assert
        assertNotNull(token);
        assertEquals("generated-jwt-token", token);
        assertEquals("hashed password", user.getPassword());
        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(userRepository, times(1)).existsByLogin(request.getLogin());
        verify(passwordEncoder, times(1)).encode(request.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtUtil, times(1)).generateToken(request.getEmail(), false);
    }

    @Test
    void testRegister_UserAlreadyExistsWithEmail() {
        // Arrange
        AuthRequestDto request = new AuthRequestDto();
        request.setLogin("testUser");
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        assertEquals("User with this email already exists", exception.getMessage());
        verify(jwtUtil, never()).generateToken(anyString(), eq(false));
    }

    @Test
    void testRegister_UserAlreadyExistsWithLogin() {
        // Arrange
        AuthRequestDto request = new AuthRequestDto();
        request.setLogin("testUser");
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.existsByLogin(request.getLogin())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        assertEquals("User with this login already exists", exception.getMessage());
        verify(jwtUtil, never()).generateToken(anyString(), eq(false));
    }

    @Test
    void testRegister_InvalidLogin() {
        // Arrange
        AuthRequestDto request = new AuthRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("password");

        // Act & Assert
        RuntimeException exception = assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        assertEquals("Login is required", exception.getMessage());
        verify(jwtUtil, never()).generateToken(anyString(), eq(false));
    }

    @Test
    void testRegister_InvalidEmail() {
        // Arrange
        AuthRequestDto request = new AuthRequestDto();
        request.setLogin("testUser");
        request.setPassword("password");

        // Act & Assert
        RuntimeException exception = assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        assertEquals("Email is required", exception.getMessage());
        verify(jwtUtil, never()).generateToken(anyString(), eq(false));
    }

    @Test
    void testRegister_InvalidPassword() {
        // Arrange
        AuthRequestDto request = new AuthRequestDto();
        request.setLogin("testUser");
        request.setEmail("test@example.com");
        request.setPassword("");

        // Act & Assert
        RuntimeException exception = assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        assertEquals("Password is required", exception.getMessage());
        verify(jwtUtil, never()).generateToken(anyString(), eq(false));
    }

    @Test
    void testLogin_Success() {
        // Arrange
        AuthRequestDto request = new AuthRequestDto();
        request.setLogin("testUser");
        request.setEmail("test@example.com");
        request.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setLogin(request.getLogin());
        user.setEmail(request.getEmail());
        user.setPassword("hashedPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(request.getEmail(), false)).thenReturn("generated-jwt-token");

        // Act
        String token = authService.login(request);

        // Assert
        assertNotNull(token);
        assertEquals("generated-jwt-token", token);
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(passwordEncoder, times(1)).matches(request.getPassword(), user.getPassword());
        verify(jwtUtil, times(1)).generateToken(request.getEmail(), false);
    }

    @Test
    void testLogin_InvalidEmail() {
        // Arrange
        AuthRequestDto request = new AuthRequestDto();
        request.setLogin("testUser");
        request.setEmail("nonexistent@example.com");
        request.setPassword("password");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        assertEquals("Invalid email", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), eq(false));
    }

    @Test
    void testLogin_InvalidLogin() {
        // Arrange
        AuthRequestDto request = new AuthRequestDto();
        request.setLogin("wrongLogin");
        request.setEmail("test@example.com");
        request.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setLogin("correctLogin");
        user.setEmail(request.getEmail());
        user.setPassword("hashedPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        // Act & Assert
        RuntimeException exception = assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        assertEquals("Invalid login", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), eq(false));
    }

    @Test
    void testLogin_InvalidPassword() {
        // Arrange
        AuthRequestDto request = new AuthRequestDto();
        request.setLogin("testUser");
        request.setEmail("test@example.com");
        request.setPassword("wrongPassword");

        User user = new User();
        user.setId(1L);
        user.setLogin(request.getLogin());
        user.setEmail(request.getEmail());
        user.setPassword("hashedPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        assertEquals("Invalid password", exception.getMessage());
        verify(jwtUtil, never()).generateToken(anyString(), eq(false));
    }

}