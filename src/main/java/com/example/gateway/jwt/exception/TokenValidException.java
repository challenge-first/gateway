package com.example.gateway.jwt.exception;

public class TokenValidException extends RuntimeException {

    public TokenValidException(String message, Throwable cause) {
        super(message, cause);
    }
}
