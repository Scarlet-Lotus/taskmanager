package com.example.taskmanager.backend.service;

import com.example.taskmanager.backend.model.User;
import com.example.taskmanager.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Loading user by email: {}", email);

        // Ищем пользователя по email в базе данных
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        log.debug("Found user with email: {} and ID: {}", user.getEmail(), user.getId());

        // Возвращаем объект UserDetails на основе данных пользователя
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(), // Логин (в данном случае email)
                user.getPassword(), // Пароль
                user.getAuthorities() // Список ролей/прав пользователя
        );

        log.info("Returning UserDetails for user with email: {}", email);
        return userDetails;
    }

}
