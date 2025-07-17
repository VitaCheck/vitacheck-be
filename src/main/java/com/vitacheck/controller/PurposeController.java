package com.vitacheck.controller;

import com.vitacheck.dto.AllPurposeDto;
import com.vitacheck.global.apiPayload.CustomResponse;
import com.vitacheck.service.PurposeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purposes")
@RequiredArgsConstructor
@Tag(name = "Purpose", description = "건강 목적 Enum API")
public class PurposeController {

    private final PurposeService purposeService;

    @GetMapping
    @Operation(
            summary = "건강 목적 전체 조회",
            description = "서버에 정의된 모든 건강 목적(enum) 리스트를 반환합니다.\n\n" +
                    "목적 드롭다운 버튼, 목적 전체 보여주기에 사용 가능합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AllPurposeDto.class)))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content
            )
    })
    public CustomResponse<List<AllPurposeDto>> getAllPurposes() {
        List<AllPurposeDto> purposes = purposeService.getAllPurposes();
        return CustomResponse.ok(purposes);
    }
}

/*
swagger 디테일 보강 전 코드 (임시 보관)
@RestController
@RequestMapping("/api/v1/purposes")
@RequiredArgsConstructor
@Tag(name = "Purpose", description = "건강 목적 Enum API")
public class PurposeController {

    private final PurposeService purposeService;

    @GetMapping
    @Operation(summary = "건강 목적 전체 조회", description = "모든 건강 목적(enum) 리스트를 반환합니다.")
    public List<AllPurposeDto> getAllPurposes() {
        return purposeService.getAllPurposes();
    }
}
 */
