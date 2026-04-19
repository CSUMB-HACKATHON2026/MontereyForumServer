package com.MCF.backend.dto.response;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private AuthUserDto user;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String refreshToken, AuthUserDto user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public AuthUserDto getUser() {
        return user;
    }

    public void setUser(AuthUserDto user) {
        this.user = user;
    }
}
