package com.heigraduate.app.graduate.exception;

public abstract class ConflictException extends RuntimeException {
    protected ConflictException(String message) {
        super(message);
    }
}