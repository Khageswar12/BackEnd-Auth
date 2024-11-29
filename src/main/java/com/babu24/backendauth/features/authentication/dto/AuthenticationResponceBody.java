package com.babu24.backendauth.features.authentication.dto;

public class AuthenticationResponceBody {
    private final String token;
    private final String message;

    public AuthenticationResponceBody(String token, String message) {
        this.token = token;
        this.message = message;
    }
    public String getToken() {
        return token;
    }
    public String getMessage() {
        return message;
    }
}
