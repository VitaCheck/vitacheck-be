package com.vitacheck.ai.config;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Configuration
public class GcpAuthConfig {

    // Functional Interface for the strategy pattern
    public interface AccessTokenProvider {
        String getAccessToken() throws IOException;
    }

    @Bean
    @Profile("local")
    public AccessTokenProvider localAccessTokenProvider() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        return () -> {
            credentials.refreshIfExpired();
            AccessToken token = credentials.getAccessToken();
            if (token == null || token.getTokenValue() == null) {
                throw new IOException("ADC (gcloud auth application-default login) setup is incorrect.");
            }
            return token.getTokenValue();
        };
    }

    @Bean
    @Profile("prod")
    public AccessTokenProvider prodAccessTokenProvider(@Value("${gcp.key-json}") String gcpKeyJsonString) throws IOException {
        if (!StringUtils.hasText(gcpKeyJsonString)) {
            throw new IOException("Could not retrieve 'gcp-key-json' from Secrets Manager.");
        }
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ByteArrayInputStream(gcpKeyJsonString.getBytes())
        ).createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        return () -> {
            credentials.refreshIfExpired();
            AccessToken token = credentials.getAccessToken();
            if (token == null || token.getTokenValue() == null) {
                throw new IOException("Failed to issue token with GCP key from Secrets Manager.");
            }
            return token.getTokenValue();
        };
    }
}