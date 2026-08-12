package com.example.backend.security;

import com.example.backend.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/*
 * JwtAuthenticationFilter
 * ------------------------
 * Runs once per request, before Spring Security decides whether to allow
 * it through. Steps:
 *   1. Read the "Authorization: Bearer <token>" header.
 *   2. Parse and verify the token's signature (JwtService).
 *   3. Mark the request as authenticated as that user/role so the rest of
 *      Spring Security (and @PreAuthorize/hasRole checks) can use it.
 *
 * If the header is missing or the token is invalid/expired, we simply
 * leave the request unauthenticated - SecurityConfig then decides whether
 * that endpoint required a login or not.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());

            try {
                Claims claims = jwtService.parseToken(token);
                String email = claims.getSubject();
                String role = claims.get("role", String.class);
                String userId = claims.get("userId", String.class);
                String name = claims.get("name", String.class);

                AuthenticatedUser principal = new AuthenticatedUser(userId, name, email, role);
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException exception) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
