package com.vitacheck.service;

import com.vitacheck.config.jwt.JwtUtil;
import com.vitacheck.domain.user.Role;
import com.vitacheck.domain.user.User;
import com.vitacheck.domain.user.UserStatus;
import com.vitacheck.dto.OAuthAttributes;
import com.vitacheck.dto.UserDto;
import com.vitacheck.global.apiPayload.CustomException;
import com.vitacheck.global.apiPayload.code.ErrorCode;
import com.vitacheck.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final NotificationSettingsService notificationSettingsService;

    @Transactional
    public void signUp(UserDto.SignUpRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }

        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // 비밀번호는 반드시 암호화하여 저장
                .fullName(request.getFullName())
                .nickname(request.getNickname())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .phoneNumber(request.getPhoneNumber())
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .provider("vitacheck") // 자체 회원가입 사용자를 명시
                .lastLoginAt(LocalDateTime.now())
                .build();
        userRepository.save(newUser);

        notificationSettingsService.createDefaultSettingsForUser(newUser);
    }

    public UserDto.TokenResponse login(UserDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        notificationSettingsService.getNotificationSettings(user.getId());

        String accessToken = jwtUtil.createAccessToken(user.getEmail());
        String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

        return new UserDto.TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public UserDto.TokenResponse socialSignUp(String tempToken, UserDto.SocialSignUpRequest request) {
        if (!jwtUtil.validateToken(tempToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }

        OAuthAttributes attributes = jwtUtil.getSocialAttributesFromToken(tempToken);

        User newUser = User.builder()
                .email(attributes.getEmail())
                .fullName(attributes.getName())
                .nickname(request.getNickname()) // 사용자가 직접 입력한 값
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .phoneNumber(request.getPhoneNumber())
                .provider(attributes.getProvider())
                .providerId(attributes.getProviderId())
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .lastLoginAt(LocalDateTime.now())
                .build();

        userRepository.save(newUser);
        notificationSettingsService.createDefaultSettingsForUser(newUser);

        String accessToken = jwtUtil.createAccessToken(newUser.getEmail());
        String refreshToken = jwtUtil.createRefreshToken(newUser.getEmail());

        return new UserDto.TokenResponse(accessToken, refreshToken);
    }

    public UserDto.InfoResponse getMyInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        int age = 0; // 기본값
        if (user.getBirthDate() != null) {
            age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();
        }
        return new UserDto.InfoResponse(
                user.getEmail(),
                user.getNickname(),
                user.getFullName(),
                user.getProvider(),
                age,
                user.getBirthDate(),
                user.getPhoneNumber()
        );
    }

    @Transactional
    public UserDto.InfoResponse updateMyInfo(String email, UserDto.UpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateInfo(request.getNickname(), request.getBirthDate(), request.getPhoneNumber());

        int age = 0; // 기본값
        if (user.getBirthDate() != null) {
            age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();
        }

        return new UserDto.InfoResponse(
                user.getEmail(),
                user.getNickname(),
                user.getFullName(),
                user.getProvider(),
                age,
                user.getBirthDate(),
                user.getPhoneNumber()
        );
    }

    public Long findIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }

    @Transactional
    public void updateFcmToken(String email, String fcmToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateFcmToken(fcmToken);
    }

    public UserDto.SocialSignUpRequest getSocialInfoFromTempToken(String tempToken) {
        if (!jwtUtil.validateToken(tempToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        // 토큰에서 OAuthAttributes 객체를 직접 추출
        OAuthAttributes attributes = jwtUtil.getSocialAttributesFromToken(tempToken);
        return new UserDto.SocialSignUpRequest(attributes);
    }
}
