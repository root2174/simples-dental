package com.simplesdental.product.controller;

import com.simplesdental.product.controller.dto.auth.AuthRequest;
import com.simplesdental.product.controller.dto.auth.AuthResponse;
import com.simplesdental.product.controller.dto.auth.UserRequest;
import com.simplesdental.product.controller.dto.auth.UserContextDTO;
import com.simplesdental.product.model.User;
import com.simplesdental.product.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

  @PostMapping("/login")
  @Operation(summary = "Login", description = "Authenticate user and return JWT token")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/register")
  @Operation(summary = "Register", description = "Register a new user")
  public ResponseEntity<User> register(@Valid @RequestBody UserRequest request) {
    return ResponseEntity.ok(authService.register(request));
  }

  @GetMapping("/context")
  @Operation(summary = "Get user context", description = "Get the current user's context")
  public ResponseEntity<UserContextDTO> getContext(Authentication authentication) {
    return ResponseEntity.ok(authService.getUserContext(authentication.getName()));
  }

  @PutMapping("/password")
  @Operation(summary = "Update password", description = "Update the current user's password")
  public ResponseEntity<Void> updatePassword(
      @RequestParam String currentPassword,
      @RequestParam String newPassword,
      Authentication authentication) {
    authService.updatePassword(authentication.getName(), currentPassword, newPassword);
    return ResponseEntity.ok().build();
  }
}
