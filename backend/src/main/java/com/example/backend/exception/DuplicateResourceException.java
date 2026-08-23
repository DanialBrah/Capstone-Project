package com.example.backend.exception;

/*
 * Thrown when a register request uses an email that already exists.
 * We check this in AuthService before saving, and the unique index on
 * AppUser.email backs it up at the database level too.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
