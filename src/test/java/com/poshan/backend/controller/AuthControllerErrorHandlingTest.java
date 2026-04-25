package com.poshan.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.poshan.backend.exception.ApiExceptionHandler;
import com.poshan.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

class AuthControllerErrorHandlingTest {

    private MockMvc mockMvc;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        mockMvc = standaloneSetup(new AuthController(authService, "http://localhost:5173"))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    }

    @Test
    void registerMemberReturnsReasonFromResponseStatusException() throws Exception {
        when(authService.registerMember(any())).thenThrow(new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Email delivery is not configured. Set MAIL_HOST, MAIL_USERNAME, MAIL_PASSWORD, and MAIL_FROM before registering users."
        ));

        mockMvc.perform(post("/api/auth/members/register")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "name": "Test User",
                      "username": "test_user",
                      "email": "test@example.com",
                      "phone": "",
                      "password": "password123"
                    }
                    """))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.message").value(
                "Email delivery is not configured. Set MAIL_HOST, MAIL_USERNAME, MAIL_PASSWORD, and MAIL_FROM before registering users."
            ))
            .andExpect(jsonPath("$.path").value("/api/auth/members/register"));
    }
}
