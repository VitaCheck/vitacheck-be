package com.vitacheck.controller;

import com.vitacheck.dto.TermsDto;
import com.vitacheck.common.CustomResponse;
import com.vitacheck.service.TermsService;
import com.vitacheck.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "terms", description = "약관 관련 API")
@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "전체 약관 목록 조회", description = "회원가입 또는 설정 화면에서 보여줄 모든 약관의 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public CustomResponse<List<TermsDto.TermResponse>> getAllTerms() {
        List<TermsDto.TermResponse> response = termsService.getAllTerms();
        return CustomResponse.ok(response);
    }

    @PostMapping("/agreements")
    @Operation(summary = "약관 동의", description = "로그인한 사용자가 아직 동의하지 않은 약관들에 대해 동의를 기록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "약관 동의 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public CustomResponse<String> agreeToTerms(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TermsDto.AgreementRequest request
    ) {
        Long userId = userService.findIdByEmail(userDetails.getUsername());
        termsService.agreeToTerms(userId, request);
        return CustomResponse.ok("약관 동의가 처리되었습니다.");
    }

    @DeleteMapping("/agreements")
    @Operation(summary = "약관 동의 철회", description = "로그인한 사용자가 이전에 동의했던 약관들의 동의를 철회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "약관 동의 철회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public CustomResponse<String> withdrawTerms(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TermsDto.AgreementWithdrawalRequest request
    ) {
        Long userId = userService.findIdByEmail(userDetails.getUsername());
        termsService.withdrawTerms(userId, request);
        return CustomResponse.ok("약관 동의가 철회되었습니다.");
    }
}