package com.simplesdental.product.service;

import com.simplesdental.product.controller.dto.auth.AuthRequest;
import com.simplesdental.product.controller.dto.auth.AuthResponse;
import com.simplesdental.product.controller.dto.auth.UserContextDTO;
import com.simplesdental.product.controller.dto.auth.UserRequest;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    User userDetails = (User) authentication.getPrincipal();
    String jwt = jwtUtils.generateToken(authentication);

    return new AuthResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getRole().name());
  }

  public User register(UserRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new RuntimeException("Email is already in use");
    }

    User user = User.builder()
        .name(request.name())
        .email(request.email())
        .password(passwordEncoder.encode(request.password()))
        .role(UserRole.USER)
        .build();

    return userRepository.save(user);
  }

  @Cacheable(value = "userContext", key = "#email")
  public UserContextDTO getUserContext(String email) {
    logger.info("Retrieving user context for email: {}", email);
    try {
      User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
      logger.info("User context retrieved successfully for: {}", email);
      return UserContextDTO.fromUser(user);
    } catch (UsernameNotFoundException e) {
      logger.warn("Failed to retrieve user context - user not found: {}", email);
      throw e;
    }
  }

  @CacheEvict(value = "userContext", key = "#email")
  @Transactional
  public void updatePassword(String email, String newPassword) {
    logger.info("Attempting to update password for user: {}", email);
    try {
      User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
      user.setPassword(passwordEncoder.encode(newPassword));
      userRepository.save(user);
      logger.info("Password updated successfully for user: {}", email);
    } catch (UsernameNotFoundException e) {
      logger.warn("Failed to update password - user not found: {}", email);
      throw e;
    } catch (Exception e) {
      logger.error("Error updating password for user {}: {}", email, e.getMessage());
      throw e;
    }
  }
}
