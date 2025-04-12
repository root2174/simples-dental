package com.simplesdental.product.controller;

import com.simplesdental.product.controller.dto.auth.AuthRequest;
import com.simplesdental.product.controller.dto.auth.AuthResponse;
import com.simplesdental.product.controller.dto.auth.UserContextDTO;
import com.simplesdental.product.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private AuthRequest authRequest;
    private AuthResponse authResponse;
    private UserContextDTO userContext;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest("test@example.com", "password123");
        authResponse = new AuthResponse("jwt-token", 1L, "test@example.com", "USER");
        userContext = UserContextDTO.builder()
            .id(1L)
            .email("test@example.com")
            .role("USER")
            .build();

        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void shouldReturnJwtTokenWhenValidCredentialsAreProvided() {
        when(authService.login(any(AuthRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.login(authRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponse, response.getBody());
        verify(authService).login(authRequest);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenAuthenticationFails() {
        when(authService.login(any(AuthRequest.class)))
            .thenThrow(new RuntimeException("Authentication failed"));

        assertThrows(RuntimeException.class, () -> authController.login(authRequest));
        verify(authService).login(authRequest);
    }

    @Test
    void shouldReturnUserContextWhenValidUserIsAuthenticated() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(authService.getUserContext(anyString())).thenReturn(userContext);

        ResponseEntity<UserContextDTO> response = authController.getContext();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userContext, response.getBody());
        verify(authService).getUserContext("test@example.com");
    }

    @Test
    void shouldThrowRuntimeExceptionWhenUserContextCannotBeRetrieved() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(authService.getUserContext(anyString()))
            .thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> authController.getContext());
        verify(authService).getUserContext("test@example.com");
    }
}
