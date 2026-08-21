package com.example.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
 * SecurityConfig
 * --------------
 * Wires Spring Security to be stateless (no sessions - JWT only) and
 * defines which endpoints are public vs. which require a logged-in user.
 *
 * Only /api/auth/register, /api/auth/login, and the API docs (Swagger UI
 * and GET /api/docs) are public. Everything else requires a valid Bearer
 * token, with role checks
 * layered on top for admin-only actions:
 * - Course writes (create/update/activate/deactivate) -> ADMIN only.
 * - Course reads -> any logged-in user (students browse courses).
 * - Creating an enrolment -> STUDENT only.
 * - Viewing every enrolment -> ADMIN only; a student can only see their
 *   own via /api/enrolments/my.
 * - Cancelling an enrolment -> any logged-in user; EnrolmentService checks
 *   that a non-admin can only cancel their own.
 * - Reports -> ADMIN only.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // /error must stay public: Spring Security's own AccessDeniedHandler
                        // responds via response.sendError(403), which makes the servlet
                        // container internally forward to /error. That forwarded request
                        // re-enters this same filter chain with no Authorization header: if
                        // /error weren't permitted, it would fall through to
                        // anyRequest().authenticated() and get rejected as 401, silently
                        // replacing the correct 403 with a misleading 401.
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/docs").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/courses").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/courses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/courses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/courses/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/enrolments").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/enrolments/my").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/enrolments").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/enrolments/**").authenticated()

                        .requestMatchers("/api/reports/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler(new AccessDeniedHandlerImpl())
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
