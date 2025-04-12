package com.simplesdental.product.security;

import com.simplesdental.product.logging.LoggerWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final LoggerWrapper log = new LoggerWrapper(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            processJwtAuthentication(request);
        } catch (Exception ex) {
            log.error("Error processing JWT authentication: {}", ex.getMessage());
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    private void processJwtAuthentication(HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        
        if (StringUtils.hasText(jwt)) {
            log.debug("JWT token found in request");
            validateAndAuthenticateToken(jwt, request);
        } else {
            log.debug("No JWT token found in request");
        }
    }

    private void validateAndAuthenticateToken(String jwt, HttpServletRequest request) {
        if (tokenProvider.validateToken(jwt)) {
            String username = tokenProvider.getUsernameFromToken(jwt);
            log.debug("Valid JWT token for user: {}", username);
            authenticateUser(username, request);
        } else {
            log.warn("Invalid JWT token received");
        }
    }

    private void authenticateUser(String username, HttpServletRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication = createAuthenticationToken(userDetails, request);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("User {} authenticated successfully", username);
    }

    private UsernamePasswordAuthenticationToken createAuthenticationToken(
            UserDetails userDetails,
            HttpServletRequest request
    ) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX_LENGTH);
        }
        return null;
    }
}
