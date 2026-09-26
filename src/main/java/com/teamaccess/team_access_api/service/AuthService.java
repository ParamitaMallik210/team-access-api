package com.teamaccess.team_access_api.service;

import com.teamaccess.team_access_api.dto.auth.SignupRequest;
import com.teamaccess.team_access_api.dto.auth.UserSummary;
import com.teamaccess.team_access_api.entity.User;
import com.teamaccess.team_access_api.exception.ApiException;
import com.teamaccess.team_access_api.repository.UserRepository;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserSummary signup(SignupRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new ApiException(
                    HttpStatus.CONFLICT.value(),
                    "Email is already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setFullName(request.fullName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        User savedUser = userRepository.save(user);

        return new UserSummary(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName());
    }
}