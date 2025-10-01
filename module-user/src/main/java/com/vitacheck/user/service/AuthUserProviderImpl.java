package com.vitacheck.user.service;

import com.vitacheck.user.dto.AuthUserDto;
import com.vitacheck.user.repository.UserRepository;
import com.vitacheck.user.service.provider.AuthUserProvider;
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
