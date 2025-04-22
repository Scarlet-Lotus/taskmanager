package com.example.taskmanager.backend.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.util.Base64;

public class JwtKeyGenerator {
    public static void main(String[] args) {
        // Генерация безопасного ключа для HS512
        byte[] secret = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();
        System.out.println("Generated Secret Key: " + Base64.getEncoder().encodeToString(secret));
    }
}
