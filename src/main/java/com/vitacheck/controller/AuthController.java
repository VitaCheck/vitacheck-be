package com.vitacheck.controller;

import com.vitacheck.dto.UserDto;
import com.vitacheck.global.apiPayload.CustomResponse;
import com.vitacheck.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "사용자 인증 API (자체/소셜)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "자체 회원가입", description = "이메일, 비밀번호 및 사용자 추가 정보를 이용해 가입을 요청합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "입력 값 형식 오류 또는 이메일 중복", content = @Content)
    })
    @PostMapping("/signup")
    public CustomResponse<String> signUp(@RequestBody UserDto.SignUpRequest request) {
        userService.signUp(request);
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

    @Operation(summary = "소셜 회원가입", description = "소셜 로그인 후 추가 정보를 받아 최종 회원가입을 처리하고 JWT를 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "소셜 회원가입 및 로그인 성공",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class))),
            @ApiResponse(responseCode = "400", description = "이미 가입된 회원이거나 필수 입력값이 누락된 경우", content = @Content)
    })
    @PostMapping("/social-signup")
    public CustomResponse<UserDto.TokenResponse> socialSignUp(@RequestBody UserDto.SocialSignUpRequest request) {
        UserDto.TokenResponse tokenResponse = userService.socialSignUp(request);
        return CustomResponse.ok(tokenResponse);
    }
}