package com.simplesdental.product.service;

import com.simplesdental.product.controller.dto.auth.AuthRequest;
import com.simplesdental.product.controller.dto.auth.AuthResponse;
import com.simplesdental.product.controller.dto.auth.UserContextDTO;
import com.simplesdental.product.controller.dto.auth.UserRequest;
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
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));

    return UserContextDTO.fromUser(user);
  }

  @CacheEvict(value = "userContext", key = "#email")
  @Transactional
  public void updatePassword(String email, String currentPassword, String newPassword) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      throw new RuntimeException("Current password is incorrect");
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }
}
