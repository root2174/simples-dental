package com.simplesdental.product.service;

import com.simplesdental.product.controller.dto.auth.AuthRequest;
import com.simplesdental.product.controller.dto.auth.AuthResponse;
import com.simplesdental.product.controller.dto.auth.UserContextDTO;
import com.simplesdental.product.controller.dto.auth.UserRequest;
import com.simplesdental.product.exception.BusinessException;
import com.simplesdental.product.exception.ResourceNotFoundException;
import com.simplesdental.product.model.User;
import com.simplesdental.product.model.UserRole;
import com.simplesdental.product.repository.UserRepository;
import com.simplesdental.product.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtUtils;

    @InjectMocks
    private AuthService authService;

    private User user;
    private AuthRequest authRequest;
    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L)
            .name("Test User")
            .email("test@example.com")
            .password("encodedPassword")
            .role(UserRole.USER)
            .build();

        authRequest = new AuthRequest("test@example.com", "password");
        userRequest = new UserRequest("Test User", "test@example.com", "password");
    }

    @Test
    void shouldReturnJwtTokenWhenValidCredentialsAreProvided() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtUtils.generateToken(any(Authentication.class))).thenReturn("jwtToken");

        AuthResponse response = authService.login(authRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.token());
        assertEquals(1L, response.id());
        assertEquals("test@example.com", response.email());
        assertEquals("USER", response.role());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateToken(any(Authentication.class));
    }

    @Test
    void shouldThrowBusinessExceptionWhenInvalidCredentialsAreProvided() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BusinessException.class, () -> authService.login(authRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldCreateUserSuccessfullyWhenValidDataIsProvided() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = authService.register(userRequest);

        assertNotNull(savedUser);
        assertEquals("Test User", savedUser.getName());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals(UserRole.USER, savedUser.getRole());
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowBusinessExceptionWhenEmailIsAlreadyInUse() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(userRequest));
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldReturnUserContextWhenValidEmailIsProvided() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        UserContextDTO context = authService.getUserContext("test@example.com");

        assertNotNull(context);
        assertEquals("test@example.com", context.email());
        assertEquals("USER", context.role());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getUserContext("test@example.com"));
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void shouldUpdatePasswordSuccessfullyWhenValidDataIsProvided() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.updatePassword("test@example.com", "newPassword");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingPasswordForNonExistentUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            authService.updatePassword("test@example.com", "newPassword"));
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }
} 
