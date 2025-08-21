package com.vitacheck.service;

import com.vitacheck.config.jwt.JwtUtil;
import com.vitacheck.domain.user.Role;
import com.vitacheck.domain.user.User;
import com.vitacheck.domain.user.UserDevice;
import com.vitacheck.domain.user.UserStatus;
import com.vitacheck.dto.OAuthAttributes;
import com.vitacheck.dto.UserDto;
import com.vitacheck.global.apiPayload.CustomException;
import com.vitacheck.global.apiPayload.code.ErrorCode;
import com.vitacheck.repository.UserDeviceRepository;
import com.vitacheck.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final NotificationSettingsService notificationSettingsService;
    private final TermsService termsService;
    private final UserDeviceRepository userDeviceRepository;

    public String preSignUp(UserDto.PreSignUpRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }
        // 필수 약관 동의 여부 검증 로직 추가 가능
        return jwtUtil.createPreSignupToken(request);
    }

    @Transactional
    public void signUp(String preSignupToken, UserDto.SignUpRequest finalRequest) {
        Claims claims = jwtUtil.getClaimsFromPreSignupToken(preSignupToken);
        String email = claims.get("email", String.class);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }

        // 1단계와 2단계 정보를 합쳐서 User 엔티티 생성
        User newUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(claims.get("password", String.class)))
                .nickname(claims.get("nickname", String.class))
                .fullName(finalRequest.getFullName()) // 2단계에서 받은 정보
                .gender(finalRequest.getGender()) // 2단계에서 받은 정보
                .birthDate(finalRequest.getBirthDate()) // 2단계에서 받은 정보
                .phoneNumber(finalRequest.getPhoneNumber()) // 2단계에서 받은 정보
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .provider("vitacheck")
                .lastLoginAt(LocalDateTime.now())
                .build();

        userRepository.saveAndFlush(newUser);

        List<?> rawList = claims.get("agreedTermIds", List.class);

        // rawList가 null인 경우를 방지하고, 비어있는 경우 예외 처리
        if (rawList == null || rawList.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        List<Long> agreedTermIds = rawList.stream()
                .map(n -> Long.valueOf(n.toString()))
                .collect(Collectors.toList());

        termsService.agreeToTerms(newUser, agreedTermIds);

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
    public void updateFcmToken(String email, UserDto.UpdateFcmTokenRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String fcmToken = request.getFcmToken();

        // 1. 혹시 다른 유저가 이 토큰을 사용하고 있었다면, 연결을 끊어줍니다.
        userDeviceRepository.findByFcmToken(fcmToken).ifPresent(device -> {
            if (!device.getUser().getId().equals(user.getId())) {
                device.updateUser(null); // 혹은 device를 삭제
            }
        });

        // 2. 현재 유저의 기기 목록에서 이미 해당 토큰이 있는지 확인합니다.
        Optional<UserDevice> existingDevice = user.getDevices().stream()
                .filter(device -> device.getFcmToken().equals(fcmToken))
                .findFirst();

        if (existingDevice.isEmpty()) {
            // 3. 없다면, 새로운 기기로 등록합니다.
            UserDevice newDevice = UserDevice.builder()
                    .user(user)
                    .fcmToken(fcmToken)
                    .deviceType(request.getDeviceType())
                    .build();
            user.getDevices().add(newDevice);
            userDeviceRepository.save(newDevice);
        }
        // 4. 이미 있다면, 아무것도 하지 않습니다 (연결 유지).
    }

    public UserDto.SocialSignUpRequest getSocialInfoFromTempToken(String tempToken) {
        if (!jwtUtil.validateToken(tempToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        // 토큰에서 OAuthAttributes 객체를 직접 추출
        OAuthAttributes attributes = jwtUtil.getSocialAttributesFromToken(tempToken);
        return new UserDto.SocialSignUpRequest(attributes);
    }
    @Transactional
    public String updateProfileImageUrl(String email, String newProfileUrl) {
        // email을 기반으로 User 엔티티를 먼저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.changeProfileUrl(newProfileUrl);
        return user.getProfileUrl();
    }

    public String getProfileImageUrlByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return user.getProfileUrl();
    }

    public UserDto.TokenResponse refreshAccessToken(UserDto.RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        String email = jwtUtil.getEmailFromToken(refreshToken);
        String newAccessToken = jwtUtil.createAccessToken(email);
        return new UserDto.TokenResponse(newAccessToken, refreshToken);
    }
    @Transactional
    public void withdrawUser(String email) {
        User user =  userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.withdraw();
    }

}
