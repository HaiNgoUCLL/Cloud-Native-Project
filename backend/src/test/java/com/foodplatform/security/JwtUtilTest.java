package com.foodplatform.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "mysecretkeymysecretkeymysecretkeymysecretkey");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void generateToken_returnsNonNull() {
        String token = jwtUtil.generateToken("user1", "test@test.com", "CUSTOMER");
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void extractUserId_returnsCorrectId() {
        String token = jwtUtil.generateToken("user1", "test@test.com", "CUSTOMER");
        assertThat(jwtUtil.extractUserId(token)).isEqualTo("user1");
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String token = jwtUtil.generateToken("user1", "test@test.com", "CUSTOMER");
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("test@test.com");
    }

    @Test
    void extractRole_returnsCorrectRole() {
        String token = jwtUtil.generateToken("user1", "test@test.com", "CUSTOMER");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("CUSTOMER");
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("user1", "test@test.com", "CUSTOMER");
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_invalidToken_returnsFalse() {
        assertThat(jwtUtil.isTokenValid("invalid.token.here")).isFalse();
    }
}
