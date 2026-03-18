package com.foodplatform.service;

import com.foodplatform.model.Order;
import com.foodplatform.model.User;
import com.foodplatform.repository.OrderRepository;
import com.foodplatform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private OrderRepository orderRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void getAllUsers_returnsList() {
        User user = new User();
        user.setId("u1");
        user.setName("Test");
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = adminService.getAllUsers();

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllOrders_returnsList() {
        Order order = new Order();
        order.setId("o1");
        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(order));

        List<Order> result = adminService.getAllOrders();

        assertThat(result).hasSize(1);
    }

    @Test
    void updateUserRole_success() {
        User user = new User();
        user.setId("u1");
        user.setRole(User.Role.CUSTOMER);
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = adminService.updateUserRole("u1", User.Role.ADMIN);

        assertThat(result.getRole()).isEqualTo(User.Role.ADMIN);
    }
}
