package com.vitacheck.auth.service;

import com.vitacheck.auth.config.jwt.JwtUtil;
import com.vitacheck.auth.dto.AuthDto;
import com.vitacheck.auth.dto.OAuthAttributes;
import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.user.domain.Role;
import com.vitacheck.user.domain.User;
import com.vitacheck.user.domain.UserStatus;
import com.vitacheck.user.dto.UserSignedUpEvent;
import com.vitacheck.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ApplicationEventPublisher applicationEventPublisher; // 이벤트 발생기

    public String preSignUp(AuthDto.PreSignUpRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }
        return jwtUtil.createPreSignupToken(request);
    }

    @Transactional
    public void signUp(String preSignupToken, AuthDto.SignUpRequest finalRequest) {
        Claims claims = jwtUtil.getClaimsFromPreSignupToken(preSignupToken);
        String email = claims.get("email", String.class);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }

        User newUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(claims.get("password", String.class)))
                .nickname(claims.get("nickname", String.class))
                .fullName(finalRequest.getFullName())
                .gender(finalRequest.getGender())
                .birthDate(finalRequest.getBirthDate())
                .phoneNumber(finalRequest.getPhoneNumber())
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .provider("vitacheck")
                .lastLoginAt(LocalDateTime.now())
                .build();

        userRepository.saveAndFlush(newUser);

        List<Long> agreeTermIds = ((List<?>) claims.get("agreeTermIds")).stream()
                .map(n -> Long.valueOf(n.toString()))
                .collect(Collectors.toList());

        applicationEventPublisher.publishEvent(new UserSignedUpEvent(newUser, agreeTermIds));
    }

    public AuthDto.TokenResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        String accessToken = jwtUtil.createAccessToken(user.getEmail());
        String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

        return new AuthDto.TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public AuthDto.TokenResponse socialSignUp(String tempToken, AuthDto.SocialSignUpRequest request) {
        if (!jwtUtil.validateToken(tempToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        OAuthAttributes attributes = jwtUtil.getSocialAttributesFromToken(tempToken);

        User newUser = User.builder()
                .email(attributes.getEmail())
                .fullName(attributes.getName())
                .nickname(request.getNickname())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .phoneNumber(request.getPhoneNumber())
                .provider(attributes.getProvider())
                .providerId(attributes.getProviderId())
                .fcmToken(request.getFcmToken())
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .lastLoginAt(LocalDateTime.now())
                .build();

        userRepository.save(newUser);

        applicationEventPublisher.publishEvent(new UserSignedUpEvent(newUser, List.of()));

        String accessToken = jwtUtil.createAccessToken(newUser.getEmail());
        String refreshToken = jwtUtil.createRefreshToken(newUser.getEmail());

        return new AuthDto.TokenResponse(accessToken, refreshToken);
    }

    public AuthDto.TokenResponse refreshAccessToken(AuthDto.RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        String email = jwtUtil.getEmailFromToken(refreshToken);
        String newAccessToken = jwtUtil.createAccessToken(email);
        return new AuthDto.TokenResponse(newAccessToken, refreshToken);
    }
}