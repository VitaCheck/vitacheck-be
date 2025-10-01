package com.vitacheck.user.service.provider;

import com.vitacheck.user.dto.CreateUserRequest;

public interface UserRegistrationService {
    void registerUser(CreateUserRequest request);
}
