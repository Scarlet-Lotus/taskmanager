package com.example.taskmanager.backend.security;

import com.example.taskmanager.backend.exception.InvalidTokenException;
import com.example.taskmanager.backend.service.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public JwtRequestFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService, HandlerExceptionResolver handlerExceptionResolver) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            if (request.getRequestURI().startsWith("/api/auth/")) {
                // Пропускаем публичные эндпоинты
                chain.doFilter(request, response);
                return;
            }

            final String authorizationHeader = request.getHeader("Authorization");

            String email;
            String jwt;

            // Извлекаем токен из заголовка Authorization
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new InsufficientAuthenticationException("Missing or invalid Authorization header");
            }

            jwt = authorizationHeader.substring(7); // Убираем "Bearer " из токена
            email = jwtUtil.extractEmail(jwt); // Извлекаем email из токена

            // Проверяем, что токен валиден и аутентификация еще не установлена
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Загружаем данные пользователя через CustomUserDetailsService
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                // Проверяем заблокированного пользователя
                if (!userDetails.isAccountNonLocked()) {
                    throw new LockedException("account is locked");
                }

                // Проверяем валидность токена относительно данных пользователя
                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Устанавливаем аутентификацию в контекст
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }

            // Продолжаем обработку запроса
            chain.doFilter(request, response);
        } catch (LockedException ex) {
            handleException(new LockedException(ex.getMessage()), request, response);
        } catch (AuthenticationException ex) {
            handleException(new InsufficientAuthenticationException(ex.getMessage()), request, response);
        } catch (MalformedJwtException ex) {
            handleException(new InvalidTokenException("Invalid token format"), request, response);
        } catch (SignatureException ex) {
            handleException(new InvalidTokenException("Invalid token signature"), request, response);
        } catch (ExpiredJwtException ex) {
            handleException(new InvalidTokenException("Token expired"), request, response);
        } catch (Exception ex) {
            handleException(new RuntimeException("Unexpected error during token validation", ex), request, response);
        }
    }

    private void handleException(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        // Передаем управление глобальному обработчику исключений
        handlerExceptionResolver.resolveException(request, response, null, ex);
    }

}
