package com.MCF.backend.service;

import com.MCF.backend.dto.response.AuthResponse;
import com.MCF.backend.exception.AuthException;
import com.MCF.backend.model.User;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
public class OAuthService {

    private static final HttpClient HTTP = HttpClient.newBuilder().build();

    private final JsonMapper jsonMapper;
    private final UserService userService;
    private final AuthService authService;

    @Value("${app.oauth2.google.client-id:}")
    private String googleClientId;

    @Value("${app.oauth2.google.client-secret:}")
    private String googleClientSecret;

    @Value("${app.oauth2.github.client-id:}")
    private String githubClientId;

    @Value("${app.oauth2.github.client-secret:}")
    private String githubClientSecret;

    public OAuthService(JsonMapper jsonMapper, UserService userService, AuthService authService) {
        this.jsonMapper = jsonMapper;
        this.userService = userService;
        this.authService = authService;
    }

    public String buildAuthorizeRedirect(String provider, String appRedirectUri, String clientState, String serverOrigin) {
        if (!isAllowedAppRedirect(appRedirectUri)) {
            throw new AuthException("redirect_uri is not allowed.");
        }
        if (clientState == null || clientState.isBlank()) {
            throw new AuthException("state is required.");
        }
        String p = provider == null ? "" : provider.trim().toLowerCase();
        String stateToken = encodeState(appRedirectUri, clientState, p);
        String origin = stripTrailingSlash(serverOrigin);
        String callback = origin + "/api/auth/oauth/callback/" + p;

        return switch (p) {
            case "google" -> buildGoogleAuthorizeUrl(callback, stateToken);
            case "github" -> buildGithubAuthorizeUrl(callback, stateToken);
            default -> throw new AuthException("Unsupported provider. Use google or github.");
        };
    }

    public String finishOAuthAndBuildAppRedirect(String provider, String code, String stateParam, String serverOrigin)
            throws IOException, InterruptedException {
        if (code == null || code.isBlank()) {
            throw new AuthException("Missing authorization code.");
        }
        Map<String, String> state = decodeState(stateParam);
        String appRedirect = state.get("a");
        String clientState = state.get("s");
        String p = state.get("p");
        if (appRedirect == null || clientState == null) {
            throw new AuthException("Invalid OAuth state.");
        }
        if (!isAllowedAppRedirect(appRedirect)) {
            throw new AuthException("redirect_uri is not allowed.");
        }
        if (p == null || !p.equalsIgnoreCase(provider)) {
            throw new AuthException("OAuth state mismatch.");
        }
        String origin = stripTrailingSlash(serverOrigin);
        String callback = origin + "/api/auth/oauth/callback/" + provider;

        String email;
        String nameHint;
        if ("google".equals(provider)) {
            String access = exchangeGoogleCode(code, callback);
            JsonNode profile = fetchGoogleProfile(access);
            email = text(profile, "email");
            nameHint = text(profile, "name");
            if (nameHint.isBlank()) {
                nameHint = text(profile, "given_name");
            }
            if (nameHint.isBlank()) {
                nameHint = email;
            }
        } else if ("github".equals(provider)) {
            String access = exchangeGithubCode(code, callback);
            JsonNode profile = fetchGithubProfile(access);
            nameHint = text(profile, "login");
            email = resolveGithubEmail(access, profile);
        } else {
            throw new AuthException("Unsupported provider.");
        }
        if (email == null || email.isBlank()) {
            throw new AuthException("Provider did not return an email address.");
        }

        User user = userService.findOrCreateOAuthUser(email, nameHint);
        AuthResponse tokens = authService.buildAuthResponse(user);

        return appRedirect
                + (appRedirect.contains("?") ? "&" : "?")
                + "status=success"
                + "&state=" + URLEncoder.encode(clientState, StandardCharsets.UTF_8)
                + "&accessToken=" + URLEncoder.encode(tokens.getAccessToken(), StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(tokens.getRefreshToken(), StandardCharsets.UTF_8)
                + "&userId=" + URLEncoder.encode(String.valueOf(user.getUserId()), StandardCharsets.UTF_8)
                + "&email=" + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8)
                + "&username=" + URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8)
                + "&role=" + URLEncoder.encode(tokens.getUser().getRole(), StandardCharsets.UTF_8);
    }

    public String buildOAuthErrorRedirect(String stateParam, String message) {
        try {
            Map<String, String> state = decodeState(stateParam);
            String appRedirect = state.get("a");
            if (appRedirect == null || appRedirect.isBlank() || !isAllowedAppRedirect(appRedirect)) {
                throw new AuthException("bad redirect");
            }
            String sep = appRedirect.contains("?") ? "&" : "?";
            return appRedirect + sep + "error=oauth&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "montereybulletin:///?error=oauth&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
        }
    }

    private static String stripTrailingSlash(String origin) {
        if (origin == null) {
            return "";
        }
        return origin.replaceAll("/+$", "");
    }

    private String encodeState(String appRedirect, String clientState, String provider) {
        Map<String, String> payload = Map.of(
                "a", appRedirect,
                "s", clientState,
                "p", provider
        );
        byte[] json = jsonMapper.writeValueAsBytes(payload);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
    }

    private Map<String, String> decodeState(String stateParam) {
        if (stateParam == null || stateParam.isBlank()) {
            throw new AuthException("Missing OAuth state.");
        }
        try {
            byte[] raw = Base64.getUrlDecoder().decode(stateParam);
            JsonNode node = jsonMapper.readTree(raw);
            return Map.of(
                    "a", text(node, "a"),
                    "s", text(node, "s"),
                    "p", text(node, "p")
            );
        } catch (IllegalArgumentException e) {
            throw new AuthException("Invalid OAuth state.");
        }
    }

    private String buildGoogleAuthorizeUrl(String redirectUri, String state) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new AuthException(
                    "Google sign-in is not set up on the server yet. Set environment variables GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET (see application.properties app.oauth2.google.*), restart the API, and add your OAuth redirect URI in Google Cloud.");
        }
        return UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", state)
                .build(true)
                .toUriString();
    }

    private String buildGithubAuthorizeUrl(String redirectUri, String state) {
        if (githubClientId == null || githubClientId.isBlank()) {
            throw new AuthException(
                    "GitHub sign-in is not set up on the server yet. Set GITHUB_CLIENT_ID and GITHUB_CLIENT_SECRET, restart the API, and add the callback URL in your GitHub OAuth app.");
        }
        return UriComponentsBuilder
                .fromUriString("https://github.com/login/oauth/authorize")
                .queryParam("client_id", githubClientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "read:user user:email")
                .queryParam("state", state)
                .build(true)
                .toUriString();
    }

    private String exchangeGoogleCode(String code, String redirectUri) throws IOException, InterruptedException {
        String body = "code=" + urlEnc(code)
                + "&client_id=" + urlEnc(googleClientId)
                + "&client_secret=" + urlEnc(googleClientSecret)
                + "&redirect_uri=" + urlEnc(redirectUri)
                + "&grant_type=authorization_code";
        HttpRequest req = HttpRequest.newBuilder(URI.create("https://oauth2.googleapis.com/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            throw new AuthException("Google token exchange failed.");
        }
        JsonNode node = jsonMapper.readTree(res.body());
        String at = text(node, "access_token");
        if (at.isBlank()) {
            throw new AuthException("Google token exchange returned no access token.");
        }
        return at;
    }

    private JsonNode fetchGoogleProfile(String accessToken) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create("https://www.googleapis.com/oauth2/v3/userinfo"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            throw new AuthException("Unable to load Google profile.");
        }
        return jsonMapper.readTree(res.body());
    }

    private String exchangeGithubCode(String code, String redirectUri) throws IOException, InterruptedException {
        String body = "code=" + urlEnc(code)
                + "&client_id=" + urlEnc(githubClientId)
                + "&client_secret=" + urlEnc(githubClientSecret)
                + "&redirect_uri=" + urlEnc(redirectUri);
        HttpRequest req = HttpRequest.newBuilder(URI.create("https://github.com/login/oauth/access_token"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            throw new AuthException("GitHub token exchange failed.");
        }
        JsonNode node = jsonMapper.readTree(res.body());
        String at = text(node, "access_token");
        if (at.isBlank()) {
            throw new AuthException("GitHub token exchange returned no access token.");
        }
        return at;
    }

    private JsonNode fetchGithubProfile(String accessToken) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.github.com/user"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github+json")
                .GET()
                .build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            throw new AuthException("Unable to load GitHub profile.");
        }
        return jsonMapper.readTree(res.body());
    }

    private String resolveGithubEmail(String accessToken, JsonNode profile) throws IOException, InterruptedException {
        String primary = text(profile, "email");
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.github.com/user/emails"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github+json")
                .GET()
                .build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            return null;
        }
        JsonNode arr = jsonMapper.readTree(res.body());
        if (!arr.isArray()) {
            return null;
        }
        for (JsonNode e : arr) {
            if (e.path("primary").asBoolean(false)) {
                return text(e, "email");
            }
        }
        for (JsonNode e : arr) {
            if (e.path("verified").asBoolean(false)) {
                return text(e, "email");
            }
        }
        return arr.size() > 0 ? text(arr.get(0), "email") : null;
    }

    private static String urlEnc(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
    }

    private static String text(JsonNode node, String field) {
        if (node == null || field == null) {
            return "";
        }
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? "" : v.asText();
    }

    private static boolean isAllowedAppRedirect(String uri) {
        if (uri == null || uri.isBlank()) {
            return false;
        }
        String u = uri.trim();
        return u.startsWith("http://localhost")
                || u.startsWith("http://127.0.0.1")
                || u.startsWith("exp://")
                || u.startsWith("montereybulletin://")
                || u.startsWith("https://");
    }
}
