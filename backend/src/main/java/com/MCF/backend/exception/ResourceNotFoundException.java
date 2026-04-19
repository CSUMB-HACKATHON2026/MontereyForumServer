package com.MCF.backend.exception;

/**
 * Exception thrown when a requested resource is not found.
 *
 * @author MCF Team
 * @version 0.1.0
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}