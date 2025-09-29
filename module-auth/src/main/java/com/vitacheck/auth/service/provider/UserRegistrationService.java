package com.vitacheck.auth.service.provider;

import com.vitacheck.auth.dto.CreateUserRequest;

public interface UserRegistrationService {
    void registerUser(CreateUserRequest request);
}
