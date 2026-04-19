package com.MCF.backend.service;

import com.MCF.backend.dto.request.CreateUserRequest;
import com.MCF.backend.dto.response.UserResponse;
import com.MCF.backend.exception.AuthException;
import com.MCF.backend.model.User;
import com.MCF.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserResponse(user);
    }

    public User requireUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User no longer exists."));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPasswordHash()));

        return new UserResponse(userRepository.save(user));
    }

    @Transactional
    public User registerWithPassword(String email, String rawPassword, String requestedUsername) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new AuthException("Email is already registered.");
        }
        String username = resolveUniqueUsername(requestedUsername, email);
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    @Transactional
    public User ensurePasswordLoginAccount(String email, String preferredUsername, String rawPassword) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    User created = new User();
                    created.setEmail(email);
                    created.setUsername(resolveUniqueUsername(preferredUsername, email));
                    return created;
                });

        if (user.getUsername() == null || user.getUsername().isBlank()) {
            user.setUsername(resolveUniqueUsername(preferredUsername, email));
        }

        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User authenticateEmailPassword(String email, String rawPassword) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AuthException("Invalid email or password."));
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new AuthException("This account uses Google or GitHub sign-in.");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new AuthException("Invalid email or password.");
        }
        return user;
    }

    @Transactional
    public User findOrCreateOAuthUser(String email, String nameHint) {
        String normalized = email.trim().toLowerCase();
        return userRepository.findByEmailIgnoreCase(normalized)
                .orElseGet(() -> {
                    String username = resolveUniqueUsername(nameHint, normalized);
                    User user = new User();
                    user.setEmail(normalized);
                    user.setUsername(username);
                    user.setPasswordHash(null);
                    return userRepository.save(user);
                });
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    private String resolveUniqueUsername(String requestedUsername, String email) {
        String base = sanitizeUsername(requestedUsername);
        if (base.isEmpty()) {
            base = sanitizeUsername(email.contains("@") ? email.substring(0, email.indexOf('@')) : email);
        }
        if (base.length() < 3) {
            base = "user" + (100 + random.nextInt(900));
        }
        String candidate = base;
        int suffix = 0;
        while (userRepository.existsByUsername(candidate)) {
            suffix++;
            candidate = base.length() > 40 ? base.substring(0, 40) + suffix : base + suffix;
        }
        return candidate;
    }

    private static String sanitizeUsername(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim().toLowerCase().replaceAll("[^a-z0-9_]", "");
        if (s.length() > 50) {
            s = s.substring(0, 50);
        }
        return s;
    }
}
