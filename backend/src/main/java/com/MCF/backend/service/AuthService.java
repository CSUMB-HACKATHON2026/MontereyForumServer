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
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{9,}$");
    private static final String PASSWORD_REQUIREMENTS_MESSAGE =
        "Password must be at least 9 characters and include 1 uppercase letter, 1 number, and 1 special character.";
        private static final String DEFAULT_ADMIN_EMAIL = "admin@admin.com";
        private static final String DEFAULT_ADMIN_USERNAME = "admin";
        private static final String DEFAULT_ADMIN_PASSWORD = "admin";

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
        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();
        String password = request.getPassword() == null ? "" : request.getPassword();

        if (email.isBlank()) {
            throw new AuthException("Email is required.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new AuthException("Email must be a valid address (example: name@domain.com).");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new AuthException(PASSWORD_REQUIREMENTS_MESSAGE);
        }
        User user = userService.registerWithPassword(
                email,
                password,
                request.getUsername()
        );
        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(EmailPasswordLoginRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();
        String password = request.getPassword() == null ? "" : request.getPassword();

        if (email.isBlank() || password.isBlank()) {
            throw new AuthException("Email and password are required.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new AuthException("Email must be a valid address (example: name@domain.com).");
        }

        if (DEFAULT_ADMIN_EMAIL.equals(email) && DEFAULT_ADMIN_PASSWORD.equals(password)) {
            userService.ensurePasswordLoginAccount(DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
        }

        User user = userService.authenticateEmailPassword(
                email,
                password
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
