package com.teamaccess.team_access_api.controller;

import com.teamaccess.team_access_api.dto.auth.SignupRequest;
import com.teamaccess.team_access_api.dto.auth.UserSummary;
import com.teamaccess.team_access_api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserSummary> signup(
            @Valid @RequestBody SignupRequest request) {
        UserSummary user = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
