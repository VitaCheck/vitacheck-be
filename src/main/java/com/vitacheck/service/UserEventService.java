package com.vitacheck.service;

import com.vitacheck.user.dto.UserSignedUpEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventService {

    private final TermsService termsService;
    private final NotificationSettingsService notificationSettingsService;

    @EventListener
    @Transactional
    public void handleUserSignedUpEvent(UserSignedUpEvent event) {
        log.info("신규 회원가입 이벤트 수신: {}", event.getUser().getEmail());

        if (event.getAgreeTermIds() != null && !event.getAgreeTermIds().isEmpty()) {
            termsService.agreeToTerms(event.getUser(), event.getAgreeTermIds());
        }

        notificationSettingsService.createDefaultSettingsForUser(event.getUser());
    }
}
