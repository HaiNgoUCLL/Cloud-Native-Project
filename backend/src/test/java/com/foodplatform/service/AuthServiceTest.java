package com.foodplatform.service;

import com.foodplatform.dto.AuthRequest;
import com.foodplatform.dto.AuthResponse;
import com.foodplatform.dto.RegisterRequest;
import com.foodplatform.model.User;
import com.foodplatform.repository.UserRepository;
import com.foodplatform.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId("user1");
        existingUser.setName("John Customer");
        existingUser.setEmail("customer@food.com");
        existingUser.setPasswordHash("$2a$encoded_password");
        existingUser.setRole(User.Role.CUSTOMER);
    }

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest("New User", "new@food.com", "Pass123!", User.Role.CUSTOMER, "123 St", "555-1234");
        when(userRepository.existsByEmail("new@food.com")).thenReturn(false);
        when(passwordEncoder.encode("Pass123!")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId("newUser1");
            return u;
        });
        when(jwtUtil.generateToken("newUser1", "new@food.com", "CUSTOMER")).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getName()).isEqualTo("New User");
        assertThat(response.getEmail()).isEqualTo("new@food.com");
        assertThat(response.getRole()).isEqualTo(User.Role.CUSTOMER);
    }

    @Test
    void register_duplicateEmail_throws() {
        RegisterRequest request = new RegisterRequest("Dup User", "customer@food.com", "Pass123!", User.Role.CUSTOMER, null, null);
        when(userRepository.existsByEmail("customer@food.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already registered");
    }

    @Test
    void login_validCredentials_returnsToken() {
        AuthRequest request = new AuthRequest("customer@food.com", "Customer123!");
        when(userRepository.findByEmail("customer@food.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("Customer123!", "$2a$encoded_password")).thenReturn(true);
        when(jwtUtil.generateToken("user1", "customer@food.com", "CUSTOMER")).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getId()).isEqualTo("user1");
    }

    @Test
    void login_invalidPassword_throws() {
        AuthRequest request = new AuthRequest("customer@food.com", "wrongPassword");
        when(userRepository.findByEmail("customer@food.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPassword", "$2a$encoded_password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email or password");
    }
}
