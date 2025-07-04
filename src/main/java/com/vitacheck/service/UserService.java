package com.vitacheck.service;

import com.vitacheck.domain.user.User;
import com.vitacheck.dto.UserDto;
import com.vitacheck.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto.InfoResponse getMyInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        return new UserDto.InfoResponse(user.getEmail(), user.getNickname());
    }

    @Transactional
    public UserDto.InfoResponse updateMyInfo(String email, UserDto.UpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        user.updateNickname(request.getNickname());
        return new UserDto.InfoResponse(user.getEmail(), user.getNickname());
    }
}
