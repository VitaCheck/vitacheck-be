package com.vitacheck.user.service;

import com.vitacheck.user.domain.Role;
import com.vitacheck.user.domain.User;
import com.vitacheck.user.domain.UserStatus;
import com.vitacheck.user.dto.CreateUserRequest;
import com.vitacheck.user.dto.UserSignedUpEvent;
import com.vitacheck.user.repository.UserRepository;
import com.vitacheck.user.service.provider.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void registerUser(CreateUserRequest request) {
        User newUser = User.builder()
                .email(request.getEmail())
                .password(request.getPassword() != null ? passwordEncoder.encode(request.getPassword()) : null)
                .nickname(request.getNickname())
                .fullName(request.getFullName())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .phoneNumber(request.getPhoneNumber())
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .provider(request.getProvider())
                .providerId(request.getProviderId())
                .fcmToken(request.getFcmToken())
                .lastLoginAt(LocalDateTime.now())
                .build();

        userRepository.saveAndFlush(newUser);

        if (request.getAgreedTermIds() != null && !request.getAgreedTermIds().isEmpty()) {
            eventPublisher.publishEvent(new UserSignedUpEvent(newUser, request.getAgreedTermIds()));
        }
    }
}