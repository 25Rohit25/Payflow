package com.stripe.payflow.application.service;

import com.stripe.payflow.api.dto.request.CreateUserRequest;
import com.stripe.payflow.api.dto.request.UpdateUserRequest;
import com.stripe.payflow.api.dto.response.UserResponse;
import com.stripe.payflow.application.service.impl.UserServiceImpl;
import com.stripe.payflow.domain.model.User;
import com.stripe.payflow.domain.repository.UserRepository;
import com.stripe.payflow.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_Success() {
        CreateUserRequest request = new CreateUserRequest("test@test.com", "John", "Doe", "pwd");
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setCreatedAt(LocalDateTime.now());
            return u;
        });

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals("test@test.com", response.email());
        assertEquals("ACTIVE", response.status());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_EmailExists() {
        CreateUserRequest request = new CreateUserRequest("test@test.com", "John", "Doe", "pwd");
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUser_Success() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setEmail("test@test.com");
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUser(id);
        assertEquals("test@test.com", response.email());
    }

    @Test
    void getUser_NotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUser(id));
    }
}
