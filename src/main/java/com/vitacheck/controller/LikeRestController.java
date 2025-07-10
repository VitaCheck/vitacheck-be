package com.vitacheck.controller;

import com.vitacheck.dto.LikeToggleResponseDto;
import com.vitacheck.global.apiPayload.CustomResponse;
import com.vitacheck.service.LikeCommandService;
import com.vitacheck.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/supplements")
public class LikeRestController {

    private final LikeCommandService likeCommandService;
    private final UserService userService;

    @Operation(summary = "영양제 찜하기", description = "사용자가 특정 영양제를 찜하거나, 이미 찜한 경우 찜을 해제합니다. (토글 방식)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "찜 토글 성공",
                    content = @Content(schema = @Schema(implementation = LikeToggleResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자 또는 영양제 없음", content = @Content)
    })
    @PostMapping("/{id}/like")
    public CustomResponse<LikeToggleResponseDto> toggleLike(
            @PathVariable("id") Long supplementId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        Long userId = userService.findIdByEmail(email);

        LikeToggleResponseDto responseDto = likeCommandService.toggleLike(supplementId, userId);
        return CustomResponse.ok(responseDto);
    }
}
