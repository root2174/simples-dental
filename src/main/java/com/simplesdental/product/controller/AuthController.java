package com.simplesdental.product.controller;

import com.simplesdental.product.controller.dto.auth.AuthRequest;
import com.simplesdental.product.controller.dto.auth.AuthResponse;
import com.simplesdental.product.controller.dto.auth.UserRequest;
import com.simplesdental.product.controller.dto.auth.UserContextDTO;
import com.simplesdental.product.logging.LoggerWrapper;
import com.simplesdental.product.model.User;
import com.simplesdental.product.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final LoggerWrapper logger = new LoggerWrapper(AuthController.class);

  @PostMapping("/login")
  @Operation(summary = "Login", description = "Authenticate user and return JWT token")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
    logger.info("Login attempt for user: {}", request.email());
    try {
      AuthResponse response = authService.login(request);
      logger.info("Login successful for user: {}", request.email());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      logger.warn("Login failed for user {}: {}", request.email(), e.getMessage());
      throw e;
    }
  }

  @PostMapping("/register")
  @Operation(summary = "Register", description = "Register a new user")
  public ResponseEntity<User> register(@Valid @RequestBody UserRequest request) {
    return ResponseEntity.ok(authService.register(request));
  }

  @GetMapping("/context")
  @Operation(summary = "Get user context", description = "Get the current user's context")
  public ResponseEntity<UserContextDTO> getContext() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    logger.info("Retrieving context for user: {}", email);
    try {
      UserContextDTO context = authService.getUserContext(email);
      logger.info("Context retrieved successfully for user: {}", email);
      return ResponseEntity.ok(context);
    } catch (Exception e) {
      logger.error("Failed to retrieve context for user {}: {}", email, e.getMessage());
      throw e;
    }
  }

  @PutMapping("/password")
  @Operation(summary = "Update password", description = "Update the current user's password")
  public ResponseEntity<Void> updatePassword(@RequestParam String newPassword) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    logger.info("Password update requested for user: {}", email);
    try {
      authService.updatePassword(email, newPassword);
      logger.info("Password updated successfully for user: {}", email);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      logger.error("Failed to update password for user {}: {}", email, e.getMessage());
      throw e;
    }
  }
}
