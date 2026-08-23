package com.example.backend.security;

/*
 * AuthenticatedUser
 * ------------------
 * Everything a controller needs about "who is making this request",
 * decoded once from the JWT by JwtAuthenticationFilter. Controllers pull
 * it in with @AuthenticationPrincipal AuthenticatedUser currentUser -
 * no extra database lookup needed per request.
 */
public record AuthenticatedUser(String id, String name, String email, String role) {
}
