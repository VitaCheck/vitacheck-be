package com.vitacheck.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AwsS3Config {

    @Value("${cloud.aws.region.static}")
    private String region;

    // 로컬 환경 전용 S3 설정
    @Configuration
    @Profile("local")
    static class LocalConfig {
        @Value("${cloud.aws.credentials.access-key}")
        private String accessKey;

        @Value("${cloud.aws.credentials.secret-key}")
        private String secretKey;

        @Value("${cloud.aws.region.static}")
        private String region;

        @Bean
        public AmazonS3 amazonS3Client() {
            AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            return AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(region)
                    .build();
        }
    }

    // 운영(prod) 환경 전용 S3 설정
    @Configuration
    @Profile("prod")
    static class ProdConfig {
        @Value("${cloud.aws.region.static}")
        private String region;

        @Bean
        public AmazonS3 amazonS3Client() {
            // Access Key 없이, EC2에 부여된 IAM 역할을 자동으로 사용합니다.
            return AmazonS3ClientBuilder.standard()
                    .withRegion(region)
                    .build();
        }
    }
}