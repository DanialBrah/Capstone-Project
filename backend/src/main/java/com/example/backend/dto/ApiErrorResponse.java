package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/*
 * ApiErrorResponse
 * ----------------
 * Standard shape for every error response the API returns, so the frontend
 * can handle all errors the same way regardless of which endpoint failed.
 */
public class ApiErrorResponse {

    private String message;
    private int status;
    private LocalDateTime timestamp;
    private List<FieldErrorDetail> errors;

    public ApiErrorResponse(String message, int status, List<FieldErrorDetail> errors) {
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }

    public String getMessage() { return message; }
    public int getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public List<FieldErrorDetail> getErrors() { return errors; }
}
