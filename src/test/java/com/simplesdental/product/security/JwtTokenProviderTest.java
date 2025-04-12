package com.simplesdental.product.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private Key key;
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long JWT_EXPIRATION = 86400000;

    @BeforeEach
    void setUp() throws Exception {
        key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        jwtTokenProvider = new JwtTokenProvider();
        
        var jwtSecretField = JwtTokenProvider.class.getDeclaredField("jwtSecret");
        jwtSecretField.setAccessible(true);
        jwtSecretField.set(jwtTokenProvider, SECRET_KEY);
        
        var jwtExpirationField = JwtTokenProvider.class.getDeclaredField("jwtExpirationInMs");
        jwtExpirationField.setAccessible(true);
        jwtExpirationField.set(jwtTokenProvider, JWT_EXPIRATION);
    }

    @Test
    void shouldGenerateValidJwtTokenWhenAuthenticationIsProvided() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.getAuthorities()).thenReturn((Collection) Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));

        String token = jwtTokenProvider.generateToken(authentication);

        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);

        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();

        assertEquals("test@example.com", claims.getSubject());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void shouldExtractUsernameFromValidJwtToken() {
        String token = Jwts.builder()
            .setSubject("test@example.com")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals("test@example.com", username);
    }

    @Test
    void shouldReturnTrueWhenValidatingValidJwtToken() {
        String token = Jwts.builder()
            .setSubject("test@example.com")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void shouldReturnFalseWhenValidatingExpiredJwtToken() {
        String token = Jwts.builder()
            .setSubject("test@example.com")
            .setIssuedAt(new Date(System.currentTimeMillis() - JWT_EXPIRATION - 1000))
            .setExpiration(new Date(System.currentTimeMillis() - 1000))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    void shouldReturnFalseWhenValidatingInvalidJwtToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
    }
} 
