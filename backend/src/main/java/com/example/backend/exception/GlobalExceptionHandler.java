package com.example.backend.exception;

import com.example.backend.dto.ApiErrorResponse;
import com.example.backend.dto.FieldErrorDetail;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/*
 * GlobalExceptionHandler
 * ----------------------
 * Central place that turns exceptions thrown anywhere in the app into
 * clean HTTP responses, so controllers only need to worry about the
 * happy path.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidationErrors(MethodArgumentNotValidException exception) {
        List<FieldErrorDetail> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();

        return new ApiErrorResponse("Validation failed", HttpStatus.BAD_REQUEST.value(), errors);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleDuplicateResource(DuplicateResourceException exception) {
        return new ApiErrorResponse(exception.getMessage(), HttpStatus.CONFLICT.value(), List.of());
    }

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleInvalidRequest(InvalidRequestException exception) {
        return new ApiErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST.value(), List.of());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleResourceNotFound(ResourceNotFoundException exception) {
        return new ApiErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND.value(), List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorResponse handleAccessDenied(AccessDeniedException exception) {
        return new ApiErrorResponse(exception.getMessage(), HttpStatus.FORBIDDEN.value(), List.of());
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleBadCredentials(RuntimeException exception) {
        return new ApiErrorResponse("Invalid email or password", HttpStatus.UNAUTHORIZED.value(), List.of());
    }

    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleJwtException(JwtException exception) {
        return new ApiErrorResponse("Invalid or expired token", HttpStatus.UNAUTHORIZED.value(), List.of());
    }
}
