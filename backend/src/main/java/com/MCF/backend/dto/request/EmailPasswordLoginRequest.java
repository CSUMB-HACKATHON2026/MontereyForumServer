package com.MCF.backend.dto.request;

public class EmailPasswordLoginRequest {
    private String email;
    private String password;

    public EmailPasswordLoginRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
