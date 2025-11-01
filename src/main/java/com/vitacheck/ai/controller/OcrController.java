package com.vitacheck.ai.controller;

import com.vitacheck.ai.dto.OcrResponseDto;
import com.vitacheck.ai.service.OcrService;
import com.vitacheck.common.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
@Tag(name = "AI Recommendation")
public class OcrController {

    private final OcrService ocrService;

    @PostMapping(
            value = "/ocr",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "AI 검색",
            description = """
        ocr로 추출한 텍스트에서 Ai가 영양제 이름과 브랜드명을 반환합니다.
        """
    )
    public CustomResponse<OcrResponseDto.OcrResult> analyzeImage(
            @Parameter(
                    description = "📷 영양제 상자 또는 제품 이미지 파일 (JPG, PNG 등)",
                    required = true,
                    example = "supplement_photo.jpg"
            )
            @RequestParam("file") MultipartFile file
    ) {
        OcrResponseDto.OcrResult result = ocrService.analyzeImageWithFastApi(file);
        return CustomResponse.ok(result);
    }
}

