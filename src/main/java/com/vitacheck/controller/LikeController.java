package com.vitacheck.controller;

import com.vitacheck.common.CustomResponse;
import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.dto.IngredientLikeToggleResponseDto;
import com.vitacheck.dto.LikeToggleResponseDto;
import com.vitacheck.dto.LikedIngredientResponseDto;
import com.vitacheck.dto.LikedSupplementResponseDto;
import com.vitacheck.service.*;
import com.vitacheck.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "likes", description = "사용자 찜 기능 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LikeController {

    private final SupplementLikeCommandService supplementLikeCommandService;
    private final UserService userService;
    private final SupplementLikeQueryService supplementLikeQueryService;
    private final IngredientLikeCommandService ingredientLikeCommandService;
    private final IngredientLikeQueryService ingredientLikeQueryService;

    @Operation(
            summary = "영양제 찜하기",
            description = "사용자가 특정 영양제를 찜하거나, 이미 찜한 경우 찜을 해제합니다. (토글 방식)",
            parameters = {
                    @Parameter(
                            name = "supplementId",
                            description = "찜할 영양제의 ID",
                            required = true,
                            example = "1"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "찜 토글 성공",
                    content = @Content(schema = @Schema(implementation = LikeToggleResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자 또는 영양제 없음", content = @Content)
    })
    @PostMapping("/supplements/{supplementId}/like")
    public CustomResponse<LikeToggleResponseDto> toggleLike(
            @PathVariable("supplementId") Long supplementId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) { // ✅ 인증 실패 시 처리 (임시방편)
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        String email = userDetails.getUsername();
        Long userId = userService.findIdByEmail(email);

        LikeToggleResponseDto responseDto = supplementLikeCommandService.toggleLike(supplementId, userId);
        return CustomResponse.ok(responseDto);
    }

    @GetMapping("/likes/me")
    @Operation(summary = "내가 찜한 영양제 목록 조회", description = "JWT 인증 기반으로 사용자가 찜한 영양제 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LikedSupplementResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    public CustomResponse<List<LikedSupplementResponseDto>> getMyLikedSupplements(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        Long userId = userService.findIdByEmail(email);

        List<LikedSupplementResponseDto> likedSupplements = supplementLikeQueryService.getLikedSupplementsByUserId(userId);
        return CustomResponse.ok(likedSupplements);
    }

    @PostMapping("/ingredients/{ingredientId}/like")
    @Operation(
            summary = "성분 찜하기",
            description = "사용자가 특정 성분을 찜하거나, 이미 찜한 경우 찜을 해제합니다. (토글 방식)",
            parameters = {
                    @Parameter(
                            name = "ingredientId",
                            description = "찜할 성분의 ID",
                            required = true,
                            example = "1"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "찜 토글 성공",
                    content = @Content(schema = @Schema(implementation = IngredientLikeToggleResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자 또는 성분 없음", content = @Content)
    })
    public CustomResponse<IngredientLikeToggleResponseDto> toggleIngredientLike(
            @PathVariable("ingredientId") Long ingredientId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        String email = userDetails.getUsername();
        Long userId = userService.findIdByEmail(email);

        IngredientLikeToggleResponseDto responseDto = ingredientLikeCommandService.toggleIngredientLike(ingredientId, userId);
        return CustomResponse.ok(responseDto);
    }

    @Operation(summary = "내가 찜한 성분 목록 조회", description = "JWT 인증 기반으로 사용자가 찜한 성분 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LikedIngredientResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    @GetMapping("/users/me/likes/ingredients")
    public CustomResponse<List<LikedIngredientResponseDto>> getMyLikedIngredients(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        Long userId = userService.findIdByEmail(email);

        List<LikedIngredientResponseDto> likedIngredients = ingredientLikeQueryService.getLikedIngredientsByUserId(userId);
        return CustomResponse.ok(likedIngredients);
    }
}
