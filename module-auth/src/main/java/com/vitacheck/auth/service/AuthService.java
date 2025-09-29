package com.vitacheck.auth.service;

import com.vitacheck.auth.config.jwt.JwtUtil;
import com.vitacheck.auth.dto.AuthDto;
import com.vitacheck.auth.dto.AuthUserDto;
import com.vitacheck.auth.dto.CreateUserRequest;
import com.vitacheck.auth.dto.OAuthAttributes;
import com.vitacheck.auth.service.provider.AuthUserProvider;
import com.vitacheck.auth.service.provider.UserRegistrationService;
import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.common.exception.CustomException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final AuthUserProvider authUserProvider;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserRegistrationService userRegistrationService;

    public String preSignUp(AuthDto.PreSignUpRequest request) {
        if (authUserProvider.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }
        return jwtUtil.createPreSignupToken(request);
    }

    @Transactional
    public void signUp(String preSignupToken, AuthDto.SignUpRequest finalRequest) {
        Claims claims = jwtUtil.getClaimsFromPreSignupToken(preSignupToken);
        String email = claims.get("email", String.class);

        if (authUserProvider.findByEmail(email).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }

        List<Long> agreedTermIds = ((List<?>) claims.get("agreedTermIds")).stream()
                .map(n -> Long.valueOf(n.toString()))
                .collect(Collectors.toList());

        CreateUserRequest createRequest = CreateUserRequest.builder()
                .email(email)
                .password(claims.get("password", String.class))
                .nickname(claims.get("nickname", String.class))
                .fullName(finalRequest.getFullName())
                .gender(finalRequest.getGender())
                .birthDate(finalRequest.getBirthDate())
                .phoneNumber(finalRequest.getPhoneNumber())
                .provider("vitacheck")
                .agreedTermIds(agreedTermIds)
                .build();

        userRegistrationService.registerUser(createRequest);
    }

    public AuthDto.TokenResponse login(AuthDto.LoginRequest request) {
        AuthUserDto user = authUserProvider.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        String accessToken = jwtUtil.createAccessToken(user); // user 객체를 직접 전달
        String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

        return new AuthDto.TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public AuthDto.TokenResponse socialSignUp(String tempToken, AuthDto.SocialSignUpRequest request) {
        if (!jwtUtil.validateToken(tempToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        OAuthAttributes attributes = jwtUtil.getSocialAttributesFromToken(tempToken);

        authUserProvider.findByEmail(attributes.getEmail()).ifPresent(u -> {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        });

        CreateUserRequest createRequest = CreateUserRequest.builder()
                .email(attributes.getEmail())
                .fullName(attributes.getName())
                .provider(attributes.getProvider())
                .providerId(attributes.getProviderId())
                .nickname(request.getNickname())
                .gender(request.getGender())
                .birthDate(LocalDate.parse(request.getBirthDate()))
                .phoneNumber(request.getPhoneNumber())
                .fcmToken(request.getFcmToken())
                .agreedTermIds(request.getAgreedTermIds())
                .build();

        userRegistrationService.registerUser(createRequest);

        // 회원가입 후 사용자 정보를 다시 조회하여 토큰 생성
        AuthUserDto newUser = authUserProvider.findByEmail(attributes.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String accessToken = jwtUtil.createAccessToken(newUser);
        String refreshToken = jwtUtil.createRefreshToken(newUser.getEmail());

        return new AuthDto.TokenResponse(accessToken, refreshToken);
    }

    public AuthDto.TokenResponse refreshAccessToken(AuthDto.RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        String email = jwtUtil.getEmailFromToken(refreshToken);

        // 이메일로 사용자 정보를 조회하여 새로운 Access Token 생성
        AuthUserDto user = authUserProvider.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtUtil.createAccessToken(user);

        return new AuthDto.TokenResponse(newAccessToken, refreshToken);
    }
}