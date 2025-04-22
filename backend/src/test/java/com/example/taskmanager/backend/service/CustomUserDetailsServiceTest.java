package com.example.taskmanager.backend.service;

import com.example.taskmanager.backend.model.User;
import com.example.taskmanager.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void testLoadUserByUsername_Success() {
        // Arrange: Создаем тестового пользователя
        String email = "test@example.com";
        String password = "encodedPassword";
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("USER");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act: Вызываем метод loadUserByUsername
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Assert: Проверяем результат
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertFalse(userDetails.getAuthorities().isEmpty());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));

        // Verify: Проверяем, что метод findByEmail был вызван
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Arrange: Настройка мока для возврата Optional.empty()
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.empty());

        // Act & Assert: Проверяем, что выбрасывается UsernameNotFoundException
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(email)
        );

        assertEquals("User not found with email: " + email, exception.getMessage());

        // Verify: Проверяем, что метод findByEmail был вызван
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testLoadUserByUsername_EmptyEmail() {
        // Arrange: Передаем пустой email
        String email = "";

        // Act & Assert: Проверяем, что выбрасывается UsernameNotFoundException
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(email)
        );

        assertEquals("User not found with email: " + email, exception.getMessage());

        // Verify: Проверяем, что метод findByEmail был вызван
        verify(userRepository, times(1)).findByEmail(email);
    }
}