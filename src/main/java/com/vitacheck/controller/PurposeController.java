package com.vitacheck.controller;

import com.vitacheck.dto.AllPurposeDto;
import com.vitacheck.service.PurposeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
