package com.vitacheck.Activity.controller;

import com.vitacheck.Activity.service.IngredientLikeCommandService;
import com.vitacheck.Activity.service.IngredientLikeQueryService;
import com.vitacheck.Activity.service.SupplementLikeCommandService;
import com.vitacheck.Activity.service.SupplementLikeQueryService;
import com.vitacheck.common.CustomResponse;
import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.Activity.dto.IngredientLikeToggleResponseDto;
import com.vitacheck.Activity.dto.LikeToggleResponseDto;
import com.vitacheck.Activity.dto.LikedIngredientResponseDto;
import com.vitacheck.Activity.dto.LikedSupplementResponseDto;
import com.vitacheck.common.security.AuthenticatedUser;
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
    private final SupplementLikeQueryService supplementLikeQueryService;
    private final IngredientLikeCommandService ingredientLikeCommandService;
    private final IngredientLikeQueryService ingredientLikeQueryService;

    @PostMapping("/supplements/{supplementId}/like")
    public CustomResponse<LikeToggleResponseDto> toggleLike(
            @PathVariable("supplementId") Long supplementId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        LikeToggleResponseDto responseDto = supplementLikeCommandService.toggleLike(supplementId, user.getUserId()); // ◀◀ user.getId() 사용
        return CustomResponse.ok(responseDto);
    }

    @GetMapping("/likes/me")
    public CustomResponse<List<LikedSupplementResponseDto>> getMyLikedSupplements(
            @AuthenticationPrincipal AuthenticatedUser user // ◀◀ 타입 변경
    ) {
        if (user == null) { throw new CustomException(ErrorCode.UNAUTHORIZED); }
        List<LikedSupplementResponseDto> likedSupplements = supplementLikeQueryService.getLikedSupplementsByUserId(user.getUserId()); // ◀◀ user.getId() 사용
        return CustomResponse.ok(likedSupplements);
    }

    @PostMapping("/ingredients/{ingredientId}/like")
    public CustomResponse<IngredientLikeToggleResponseDto> toggleIngredientLike(
            @PathVariable("ingredientId") Long ingredientId,
            @AuthenticationPrincipal AuthenticatedUser user // ◀◀ 타입 변경
    ) {
        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        IngredientLikeToggleResponseDto responseDto = ingredientLikeCommandService.toggleIngredientLike(ingredientId, user.getUserId()); // ◀◀ user.getId() 사용
        return CustomResponse.ok(responseDto);
    }

    @GetMapping("/users/me/likes/ingredients")
    public CustomResponse<List<LikedIngredientResponseDto>> getMyLikedIngredients(
            @AuthenticationPrincipal AuthenticatedUser user // ◀◀ 타입 변경
    ) {
        if (user == null) { throw new CustomException(ErrorCode.UNAUTHORIZED); }
        List<LikedIngredientResponseDto> likedIngredients = ingredientLikeQueryService.getLikedIngredientsByUserId(user.getUserId()); // ◀◀ user.getId() 사용
        return CustomResponse.ok(likedIngredients);
    }
}