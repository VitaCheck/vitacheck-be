package com.vitacheck.user.controller;

import com.vitacheck.common.CustomResponse;
import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.common.security.AuthenticatedUser;
import com.vitacheck.user.dto.UserDto;
import com.vitacheck.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "user", description = "사용자 정보 관련 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "인증된 사용자의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserDto.InfoResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/me")
    public CustomResponse<UserDto.InfoResponse> getMyInfo(@AuthenticationPrincipal AuthenticatedUser user){
        if (user == null) { throw new CustomException(ErrorCode.UNAUTHORIZED); }
        UserDto.InfoResponse myInfo = userService.getMyInfo(user.getEmail()); // ◀◀ user.getEmail() 사용
        return CustomResponse.ok(myInfo);
    }

    @Operation(summary = "내 정보 수정", description = "사용자의 닉네임, 생년월일, 휴대폰 번호를 수정합니다.") // 설명 수정
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = UserDto.InfoResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    @PutMapping("/me")
    public CustomResponse<UserDto.InfoResponse> updateMyInfo(
            @AuthenticationPrincipal AuthenticatedUser user,

            // Swagger 예시를 직접 지정하는 어노테이션 추가
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "변경할 사용자 정보를 전달합니다. 변경을 원하지 않는 필드는 생략 가능합니다.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UserDto.UpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "사용자 정보 수정 예시",
                                    value = "{\"nickname\": \"새로운행복쿼카\", \"birthDate\": \"2000-02-20\", \"phoneNumber\": \"010-9876-5432\"}"
                            )
                    )
            )
            @RequestBody UserDto.UpdateRequest request
    ) {
        if (user == null) { throw new CustomException(ErrorCode.UNAUTHORIZED); }
        UserDto.InfoResponse updatedInfo = userService.updateMyInfo(user.getEmail(), request); // ◀◀ user.getEmail() 사용
        return CustomResponse.ok(updatedInfo);
    }
    

    @Operation(summary = "프로필 사진 URL 업데이트", description = "사용자 본인의 프로필 사진을 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 URL 업데이트 성공",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":true,\"code\":\"COMMON200\",\"message\":\"성공적으로 요청을 수행했습니다.\",\"result\":\"FCM 토큰이 업데이트되었습니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":false,\"code\":\"U0001\",\"message\":\"로그인이 필요합니다.\",\"result\":null}"))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":false,\"code\":\"U0002\",\"message\":\"사용자를 찾을 수 없습니다.\",\"result\":null}")))
    })
    @PatchMapping("/me/profile-image")
    public CustomResponse<String> updateProfileImage(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UserDto.ProfileUpdateRequest request
    ){
        if (user == null) { throw new CustomException(ErrorCode.UNAUTHORIZED); }
        String newImageUrl = userService.updateProfileImageUrl(user.getEmail(), request.getProfileImageUrl()); // ◀◀ user.getEmail() 사용
        return CustomResponse.ok(newImageUrl);
    }

    @Operation(summary = "FCM 토큰 업데이트", description = "클라이언트(앱/웹)의 푸시 알림을 위한 FCM 디바이스 토큰을 등록 또는 갱신합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "FCM 토큰 업데이트 성공",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":true,\"code\":\"COMMON200\",\"message\":\"성공적으로 요청을 수행했습니다.\",\"result\":\"FCM 토큰이 업데이트되었습니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":false,\"code\":\"U0001\",\"message\":\"로그인이 필요합니다.\",\"result\":null}"))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":false,\"code\":\"U0002\",\"message\":\"사용자를 찾을 수 없습니다.\",\"result\":null}")))
    })
    @PutMapping("/me/fcm-token")
    public CustomResponse<String> updateFcmToken(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "사용자의 새로운 FCM 디바이스 토큰")
            @RequestBody UserDto.UpdateFcmTokenRequest request
    ) {
        if (user == null) { throw new CustomException(ErrorCode.UNAUTHORIZED); }
        userService.updateFcmToken(user.getEmail(), request.getFcmToken()); // ◀◀ user.getEmail() 사용
        return CustomResponse.ok("FCM 토큰이 업데이트되었습니다.");
    }

    @Operation(summary = "내 프로필 사진 URL 조회", description = "인증된 사용자의 프로필 사진 URL을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(type = "string", example = "https://your-s3-bucket/path/to/image.jpg"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/me/profile-image")
    public CustomResponse<String> getMyProfileImageUrl(@AuthenticationPrincipal AuthenticatedUser user) {
        if (user == null) { throw new CustomException(ErrorCode.UNAUTHORIZED); }
        String profileImageUrl = userService.getProfileImageUrlByEmail(user.getEmail()); // ◀◀ user.getEmail() 사용
        return CustomResponse.ok(profileImageUrl);
    }

    @Operation(summary = "회원 탈퇴", description = "로그인된 사용자의 계정을 탈퇴 처리합니다. 데이터는 30일 후 영구 삭제됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 요청 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @DeleteMapping("/me")
    public CustomResponse<String> withdrawUser(@AuthenticationPrincipal AuthenticatedUser user) {
        if (user == null) { throw new CustomException(ErrorCode.UNAUTHORIZED); }
        userService.withdrawUser(user.getEmail()); // ◀◀ user.getEmail() 사용
        return CustomResponse.ok("회원 탈퇴 요청이 처리되었습니다.");
    }
}
