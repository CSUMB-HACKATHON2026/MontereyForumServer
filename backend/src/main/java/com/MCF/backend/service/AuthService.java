package com.MCF.backend.service;

import com.MCF.backend.dto.request.EmailPasswordLoginRequest;
import com.MCF.backend.dto.request.EmailPasswordRegisterRequest;
import com.MCF.backend.dto.request.RefreshTokenRequest;
import com.MCF.backend.dto.response.AuthResponse;
import com.MCF.backend.dto.response.AuthUserDto;
import com.MCF.backend.exception.AuthException;
import com.MCF.backend.model.User;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;

    @Value("${app.security.admin-emails:}")
    private String adminEmailsRaw;

    public AuthService(UserService userService, JwtTokenService jwtTokenService) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthResponse register(EmailPasswordRegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new AuthException("Email is required.");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new AuthException("Password must be at least 8 characters.");
        }
        User user = userService.registerWithPassword(
                request.getEmail().trim().toLowerCase(),
                request.getPassword(),
                request.getUsername()
        );
        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(EmailPasswordLoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new AuthException("Email and password are required.");
        }
        User user = userService.authenticateEmailPassword(
                request.getEmail().trim().toLowerCase(),
                request.getPassword()
        );
        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw new AuthException("Refresh token is required.");
        }
        final Claims claims;
        try {
            claims = jwtTokenService.parseAndValidate(request.getRefreshToken(), "refresh");
        } catch (Exception e) {
            throw new AuthException("Invalid or expired refresh token.");
        }
        long userId = Long.parseLong(claims.getSubject());
        User user = userService.requireUserById(userId);
        return buildAuthResponse(user);
    }

    public AuthResponse buildAuthResponse(User user) {
        String role = resolveAppRole(user.getEmail());
        String access = jwtTokenService.createAccessToken(user.getUserId(), user.getUsername(), role);
        String refresh = jwtTokenService.createRefreshToken(user.getUserId(), user.getUsername(), role);
        AuthUserDto dto = new AuthUserDto(
                String.valueOf(user.getUserId()),
                user.getEmail(),
                user.getUsername(),
                role
        );
        return new AuthResponse(access, refresh, dto);
    }

    private String resolveAppRole(String email) {
        if (email == null || adminEmailsRaw == null || adminEmailsRaw.isBlank()) {
            return "CIVILIAN";
        }
        String normalized = email.trim().toLowerCase();
        return Arrays.stream(adminEmailsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .anyMatch(normalized::equals) ? "ADMIN" : "CIVILIAN";
    }
}
