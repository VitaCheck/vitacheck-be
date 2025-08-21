package com.vitacheck.service;

// OcrProxyService.java
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OcrProxyService {

    private final WebClient webClient = WebClient.builder().build();

    @Value("${ncloud.ocr.url}")
    private String ocrUrl;

    @Value("${ncloud.ocr.secret}")
    private String ocrSecret;

    public Mono<String> forwardToNcloud(Object payload) {
        return webClient.post()
                .uri(ocrUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-OCR-SECRET", ocrSecret)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class);
    }
}
