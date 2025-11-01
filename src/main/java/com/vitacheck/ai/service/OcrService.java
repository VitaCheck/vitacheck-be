package com.vitacheck.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitacheck.ai.dto.OcrResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class OcrService {

    private final WebClient fastApiWebClient;  // ✅ config에서 주입됨
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OcrResponseDto.OcrResult analyzeImageWithFastApi(MultipartFile multipartFile) {
        try {
            File tempFile = File.createTempFile("upload-", multipartFile.getOriginalFilename());
            multipartFile.transferTo(tempFile);

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new FileSystemResource(tempFile));

            String response = fastApiWebClient.post()
                    .uri("api/v1/ocr/analyze")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            tempFile.delete();

            // ✅ JSON 문자열 파싱
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode resultNode = jsonNode.get("result");

            // Gemini가 반환한 문자열 안에 json이 들어있는 경우 대비
            String cleaned = resultNode.asText()
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            JsonNode data = objectMapper.readTree(cleaned);

            return OcrResponseDto.OcrResult.builder()
                    .name(data.get("name").asText(""))
                    .brand(data.get("brand").asText(""))
//                    .ingredients(data.get("ingredients").asText(""))
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("이미지 전송 또는 응답 파싱 중 오류 발생", e);
        }
    }
}
