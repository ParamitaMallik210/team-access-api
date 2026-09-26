package com.teamaccess.team_access_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.teamaccess.team_access_api.dto.auth.SignupRequest;
import com.teamaccess.team_access_api.dto.auth.UserSummary;
import com.teamaccess.team_access_api.exception.GlobalExceptionHandler;
import com.teamaccess.team_access_api.security.SecurityConfig;
import com.teamaccess.team_access_api.service.AuthService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void signupReturnsCreatedAndSafeUserDetails() throws Exception {
        when(authService.signup(any(SignupRequest.class)))
                .thenReturn(new UserSummary(
                        UUID.fromString("11111111-1111-1111-1111-111111111111"),
                        "alex@example.com",
                        "Alex"));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "alex@example.com",
                                  "password": "LongPassword123",
                                  "fullName": "Alex"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("alex@example.com"))
                .andExpect(jsonPath("$.fullName").value("Alex"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(authService).signup(any(SignupRequest.class));
    }

    @Test
    void signupReturnsBadRequestForInvalidInput() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "password": "short",
                                  "fullName": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists())
                .andExpect(jsonPath("$.fieldErrors.fullName").exists());
    }
}
