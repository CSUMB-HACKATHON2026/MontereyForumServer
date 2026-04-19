package com.MCF.backend.controller;

import com.MCF.backend.dto.request.EmailPasswordLoginRequest;
import com.MCF.backend.dto.request.EmailPasswordRegisterRequest;
import com.MCF.backend.dto.request.RefreshTokenRequest;
import com.MCF.backend.dto.response.AuthResponse;
import com.MCF.backend.exception.AuthException;
import com.MCF.backend.service.AuthService;
import com.MCF.backend.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final AuthService authService;
    private final OAuthService oauthService;

    @Value("${app.public-origin:}")
    private String configuredPublicOrigin;

    public AuthController(AuthService authService, OAuthService oauthService) {
        this.authService = authService;
        this.oauthService = oauthService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody EmailPasswordRegisterRequest body) {
        return authService.register(body);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody EmailPasswordLoginRequest body) {
        return authService.login(body);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshTokenRequest body) {
        return authService.refresh(body);
    }

    @GetMapping("/oauth/start")
    public ResponseEntity<Void> oauthStart(
            @RequestParam String provider,
            @RequestParam(name = "redirect_uri") String redirectUri,
            @RequestParam String state,
            HttpServletRequest request
    ) {
        String origin = resolveServerOrigin(request);
        try {
            String location = oauthService.buildAuthorizeRedirect(provider, redirectUri, state, origin);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(location)).build();
        } catch (AuthException e) {
            String sep = redirectUri.contains("?") ? "&" : "?";
            String target = redirectUri + sep
                    + "error=oauth"
                    + "&message=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8)
                    + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
        }
    }

    @GetMapping("/oauth/callback/{provider}")
    public ResponseEntity<Void> oauthCallback(
            @PathVariable String provider,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            HttpServletRequest request
    ) {
        String origin = resolveServerOrigin(request);
        try {
            String target = oauthService.finishOAuthAndBuildAppRedirect(
                    provider.toLowerCase(),
                    code,
                    state,
                    origin
            );
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
        } catch (Exception e) {
            String message = e instanceof AuthException ? e.getMessage() : "OAuth failed.";
            String redirect = oauthService.buildOAuthErrorRedirect(state, message);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirect)).build();
        }
    }

    private String resolveServerOrigin(HttpServletRequest request) {
        if (configuredPublicOrigin != null && !configuredPublicOrigin.isBlank()) {
            return configuredPublicOrigin.replaceAll("/+$", "");
        }
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        StringBuilder sb = new StringBuilder(scheme).append("://").append(host);
        if (("http".equals(scheme) && port != 80) || ("https".equals(scheme) && port != 443)) {
            sb.append(":").append(port);
        }
        return sb.toString();
    }
}
