package com.stripe.payflow.application.service.impl;

import com.stripe.payflow.api.dto.request.CreateUserRequest;
import com.stripe.payflow.api.dto.request.UpdateUserRequest;
import com.stripe.payflow.api.dto.response.UserResponse;
import com.stripe.payflow.application.service.UserService;
import com.stripe.payflow.domain.model.User;
import com.stripe.payflow.domain.repository.UserRepository;
import com.stripe.payflow.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }
        
        User user = new User();
        user.setId(UUID.randomUUID());
        String[] nameParts = request.name().split(" ", 2);
        user.setFirstName(nameParts[0]);
        user.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        user.setEmail(request.email());
        user.setStatus("ACTIVE");
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        user = userRepository.save(user);
        return new UserResponse(user.getId(), user.getFirstName() + " " + user.getLastName(), user.getEmail(), user.getCreatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return new UserResponse(user.getId(), user.getFirstName() + " " + user.getLastName(), user.getEmail(), user.getCreatedAt());
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
                
        if (request.name() != null) {
            String[] nameParts = request.name().split(" ", 2);
            user.setFirstName(nameParts[0]);
            user.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        }
        
        if (request.email() != null) {
            user.setEmail(request.email());
        }
        
        user = userRepository.save(user);
        return new UserResponse(user.getId(), user.getFirstName() + " " + user.getLastName(), user.getEmail(), user.getCreatedAt());
    }
}
