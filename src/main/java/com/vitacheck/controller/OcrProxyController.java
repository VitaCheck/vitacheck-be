package com.vitacheck.controller;

// OcrProxyController.java
import com.vitacheck.service.OcrProxyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clova-ocr")
public class OcrProxyController {

    private final OcrProxyService ocrProxyService;

    @PostMapping("/infer")
    @CrossOrigin(origins = {
            "https://vitachecking.com",
            "https://vita-check.com",
            "http://localhost:5173"
    })
    public Mono<ResponseEntity<String>> infer(@RequestBody Object body) {
        return ocrProxyService.forwardToNcloud(body)
                .map(ResponseEntity::ok);
    }
}

