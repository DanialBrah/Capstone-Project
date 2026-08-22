package com.example.backend.service;

import com.example.backend.dto.AuthResponse;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.exception.DuplicateResourceException;
import com.example.backend.model.AppUser;
import com.example.backend.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerCreatesStudentWithNormalizedEmailAndReturnsToken() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Ada Lovelace");
        request.setEmail("Ada@Example.com");
        request.setPassword("password123");

        when(appUserRepository.existsByEmailIgnoreCase("ada@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser saved = invocation.getArgument(0);
            saved.setId("user-1");
            return saved;
        });
        when(jwtService.generateToken(any(AppUser.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationMinutes()).thenReturn(60L);

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUserId()).isEqualTo("user-1");

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());
        AppUser saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("ada@example.com");
        assertThat(saved.getRole()).isEqualTo("STUDENT");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed-password");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Ada Lovelace");
        request.setEmail("ada@example.com");
        request.setPassword("password123");

        when(appUserRepository.existsByEmailIgnoreCase("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(appUserRepository, never()).save(any());
    }

    @Test
    void loginAuthenticatesAndReturnsTokenForExistingUser() {
        LoginRequest request = new LoginRequest();
        request.setEmail("Ada@Example.com");
        request.setPassword("password123");

        AppUser user = new AppUser("Ada Lovelace", "ada@example.com", "hashed-password", "STUDENT");
        user.setId("user-1");

        when(appUserRepository.findByEmailIgnoreCase("ada@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationMinutes()).thenReturn(60L);

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("STUDENT");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void loginPropagatesBadCredentialsWithoutIssuingToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("ada@example.com");
        request.setPassword("wrong-password");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(BadCredentialsException.class);
        verify(jwtService, never()).generateToken(any());
    }
}
