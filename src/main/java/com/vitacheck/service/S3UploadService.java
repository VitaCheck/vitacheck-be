package com.vitacheck.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.vitacheck.global.apiPayload.CustomException;
import com.vitacheck.global.apiPayload.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3UploadService {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file, String dirName) {
        String original = Optional.ofNullable(file.getOriginalFilename())
                .filter(name -> !name.isBlank())
                .orElseThrow(() -> new IllegalArgumentException("파일 이름이 없습니다."));
        String uniqueName = UUID.randomUUID() + "_" + original.replaceAll("[^a-zA-Z0-9.\\-]", "_");
        String key = dirName + "/" + uniqueName;

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        Optional.ofNullable(file.getContentType()).ifPresent(meta::setContentType);
        meta.setCacheControl("public, max-age=31536000");

        try (InputStream in = file.getInputStream()) {
            amazonS3.putObject(bucket, key, in, meta);
        } catch (SdkClientException | IOException e) {
            throw new CustomException(ErrorCode.S3_UPLOAD_ERROR);
        }

        return amazonS3.getUrl(bucket, key).toString();
    }
}
