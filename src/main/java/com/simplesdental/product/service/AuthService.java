package com.simplesdental.product.service;

import com.simplesdental.product.controller.dto.auth.AuthRequest;
import com.simplesdental.product.controller.dto.auth.AuthResponse;
import com.simplesdental.product.controller.dto.auth.UserContextDTO;
import com.simplesdental.product.controller.dto.auth.UserRequest;
import com.simplesdental.product.exception.BusinessException;
import com.simplesdental.product.exception.ResourceNotFoundException;
import com.simplesdental.product.logging.LoggerWrapper;
import com.simplesdental.product.model.User;
import com.simplesdental.product.model.UserRole;
import com.simplesdental.product.repository.UserRepository;
import com.simplesdental.product.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtUtils;
    private final LoggerWrapper logger = new LoggerWrapper(AuthService.class);

    public AuthResponse login(AuthRequest request) {
        logger.info("Attempting login for user: {}", request.email());
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User userDetails = (User) authentication.getPrincipal();
            String jwt = jwtUtils.generateToken(authentication);

            logger.info("Login successful for user: {}", request.email());
            return new AuthResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getRole().name());
        } catch (Exception e) {
            logger.error("Login failed for user {}: {}", request.email(), e.getMessage());
            throw new BusinessException("Authentication failed", e);
        }
    }

    public User register(UserRequest request) {
        logger.info("Attempting to register new user: {}", request.email());
        try {
            if (userRepository.existsByEmail(request.email())) {
                logger.warn("Registration failed - email already in use: {}", request.email());
                throw new BusinessException("Email is already in use");
            }

            User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .build();

            User savedUser = userRepository.save(user);
            logger.info("User registered successfully: {}", request.email());
            return savedUser;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error registering user {}: {}", request.email(), e.getMessage());
            throw new BusinessException("Error registering user", e);
        }
    }

    @Cacheable(value = "userContext", key = "#email")
    public UserContextDTO getUserContext(String email) {
        logger.info("Retrieving user context for email: {}", email);
        try {
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new ResourceNotFoundException("User", "email", email);
                });
            logger.info("User context retrieved successfully for: {}", email);
            return UserContextDTO.fromUser(user);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving user context for {}: {}", email, e.getMessage());
            throw new BusinessException("Error retrieving user context", e);
        }
    }

    @CacheEvict(value = "userContext", key = "#email")
    @Transactional
    public void updatePassword(String email, String newPassword) {
        logger.info("Attempting to update password for user: {}", email);
        try {
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new ResourceNotFoundException("User", "email", email);
                });
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            logger.info("Password updated successfully for user: {}", email);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating password for user {}: {}", email, e.getMessage());
            throw new BusinessException("Error updating password", e);
        }
    }
}
