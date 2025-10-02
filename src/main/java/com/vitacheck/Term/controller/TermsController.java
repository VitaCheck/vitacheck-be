package com.vitacheck.Term.controller;

import com.vitacheck.Term.dto.TermsDto;
import com.vitacheck.common.CustomResponse;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.common.security.AuthenticatedUser; // ◀◀ import 변경
import com.vitacheck.Term.service.TermsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "terms", description = "약관 관련 API")
@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;
    // private final UserService userService; // ◀◀ 더 이상 필요 없음

    @GetMapping
    @Operation(summary = "전체 약관 목록 조회", description = "...")
    public CustomResponse<List<TermsDto.TermResponse>> getAllTerms() {
        List<TermsDto.TermResponse> response = termsService.getAllTerms();
        return CustomResponse.ok(response);
    }

    @PostMapping("/agreements")
    @Operation(summary = "약관 동의", description = "...")
    public CustomResponse<String> agreeToTerms(
            @AuthenticationPrincipal AuthenticatedUser user, // ◀◀ 타입 변경
            @Valid @RequestBody TermsDto.AgreementRequest request
    ) {
        // user가 null인지 체크하는 로직 추가 (더 안전)
        if (user == null) {
            // 이 코드는 JwtAuthenticationFilter가 정상 동작하면 실행되지 않아야 합니다.
            // 하지만 방어적인 코드로 남겨두는 것이 좋습니다.
            throw new CustomException(com.vitacheck.common.code.ErrorCode.UNAUTHORIZED);
        }

        // email 대신 user ID를 직접 사용
        termsService.agreeToTerms(user.getUserId(), request);
        return CustomResponse.ok("약관 동의가 처리되었습니다.");
    }

    @DeleteMapping("/agreements")
    @Operation(summary = "약관 동의 철회", description = "...")
    public CustomResponse<String> withdrawTerms(
            @AuthenticationPrincipal AuthenticatedUser user, // ◀◀ 타입 변경
            @Valid @RequestBody TermsDto.AgreementWithdrawalRequest request
    ) {
        if (user == null) {
            throw new CustomException(com.vitacheck.common.code.ErrorCode.UNAUTHORIZED);
        }

        // email 대신 user ID를 직접 사용
        termsService.withdrawTerms(user.getUserId(), request);
        return CustomResponse.ok("약관 동의가 철회되었습니다.");
    }
}