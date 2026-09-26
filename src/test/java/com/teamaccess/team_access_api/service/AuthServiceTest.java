package com.teamaccess.team_access_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.teamaccess.team_access_api.dto.auth.SignupRequest;
import com.teamaccess.team_access_api.entity.User;
import com.teamaccess.team_access_api.exception.ApiException;
import com.teamaccess.team_access_api.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void signupNormalizesEmailHashesPasswordAndReturnsSafeSummary() {
        SignupRequest request =
                new SignupRequest(" Alex@Example.com ", "LongPassword123", "Alex");

        when(userRepository.findByEmail("alex@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("LongPassword123"))
                .thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });

        var result = authService.signup(request);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());

        assertEquals("alex@example.com", savedUser.getValue().getEmail());
        assertEquals("bcrypt-hash", savedUser.getValue().getPasswordHash());
        assertEquals("alex@example.com", result.email());
        assertEquals("Alex", result.fullName());
        verify(passwordEncoder).encode("LongPassword123");
    }

    @Test
    void signupRejectsAnAlreadyRegisteredEmail() {
        SignupRequest request =
                new SignupRequest("alex@example.com", "LongPassword123", "Alex");

        when(userRepository.findByEmail("alex@example.com"))
                .thenReturn(Optional.of(new User()));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.signup(request));

        assertEquals(HttpStatus.CONFLICT.value(), exception.getStatus());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }
}
