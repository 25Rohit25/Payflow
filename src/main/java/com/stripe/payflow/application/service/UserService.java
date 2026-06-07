package com.stripe.payflow.application.service;

import com.stripe.payflow.api.dto.request.CreateUserRequest;
import com.stripe.payflow.api.dto.request.UpdateUserRequest;
import com.stripe.payflow.api.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUser(UUID id);
    UserResponse updateUser(UUID id, UpdateUserRequest request);
}
