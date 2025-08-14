package com.vitacheck.controller;

import com.vitacheck.dto.UserDto;
import com.vitacheck.global.apiPayload.CustomResponse;
import com.vitacheck.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "사용자 인증 API (자체/소셜)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "1단계 임시 회원가입 (임시 토큰 발급)", description = "이메일, 비밀번호, 닉네임, 약관 동의 내역을 받아 2단계에서 사용할 임시 토큰을 발급합니다.")
    @PostMapping("/pre-signup")
    public CustomResponse<String> preSignUp(@RequestBody @Valid UserDto.PreSignUpRequest request) {
        String preSignupToken = userService.preSignUp(request);
        return CustomResponse.ok(preSignupToken);
    }

    @Operation(summary = "2단계 최종 회원가입", description = "이름, 성별, 생년월일, 전화번호와 임시 토큰을 받아 최종 회원가입을 완료합니다.")
    @PostMapping("/signup")
    public CustomResponse<String> signUp(
            @Parameter(description = "1단계에서 발급받은 임시 토큰") @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody @Valid UserDto.SignUpRequest finalRequest
    ) {
        String preSignupToken = authorizationHeader.substring(7); // "Bearer " 제거
        userService.signUp(preSignupToken, finalRequest);
        return CustomResponse.created("회원가입이 성공적으로 완료되었습니다.");
    }

    @Operation(summary = "자체 로그인", description = "이메일과 비밀번호로 로그인하고 API 접근을 위한 JWT를 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class))),
            @ApiResponse(responseCode = "404", description = "가입되지 않은 회원이거나 비밀번호가 틀린 경우", content = @Content)
    })
    @PostMapping("/login")
    public CustomResponse<UserDto.TokenResponse> login(@RequestBody UserDto.LoginRequest request) {
        UserDto.TokenResponse tokenResponse = userService.login(request);
        return CustomResponse.ok(tokenResponse);
    }

    @Operation(summary = "소셜 회원가입", description = "소셜 로그인 후 임시 토큰과 추가 정보를 받아 최종 회원가입을 처리하고 JWT를 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "소셜 회원가입 및 로그인 성공",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class))),
            @ApiResponse(responseCode = "400", description = "이미 가입된 회원이거나 필수 입력값이 누락된 경우", content = @Content),
            @ApiResponse(responseCode = "401", description = "임시 토큰이 유효하지 않거나 만료된 경우", content = @Content) // 401 응답 설명 추가
    })
    @PostMapping("/social-signup")
    public CustomResponse<UserDto.TokenResponse> socialSignUp(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody UserDto.SocialSignUpRequest request
    ) {
        String tempToken = authorizationHeader.substring(7);
        UserDto.TokenResponse tokenResponse = userService.socialSignUp(tempToken, request);
        return CustomResponse.ok(tokenResponse);
    }
}