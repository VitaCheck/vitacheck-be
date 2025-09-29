package com.vitacheck.user.service;

import com.vitacheck.auth.dto.AuthUserDto;
import com.vitacheck.auth.service.provider.AuthUserProvider;
import com.vitacheck.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthUserProviderImpl implements AuthUserProvider {

    private final UserRepository userRepository;

    @Override
    public Optional<AuthUserDto> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> new AuthUserDto(
                        user.getId(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getRole().name(),
                        user.getGender().name(),
                        user.getBirthDate()
                ));
    }


}
