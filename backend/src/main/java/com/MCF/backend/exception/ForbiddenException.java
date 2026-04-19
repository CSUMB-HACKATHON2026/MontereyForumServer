package com.MCF.backend.exception;

/**
 * Exception thrown when a user attempts to access or modify a resource they do not own.
 *
 * @author MCF Team
 * @version 0.1.0
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}