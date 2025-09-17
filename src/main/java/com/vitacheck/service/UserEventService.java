package com.vitacheck.service;

import com.vitacheck.domain.Terms;
import com.vitacheck.domain.mapping.UserTermsAgreement;
import com.vitacheck.repository.TermsRepository;
import com.vitacheck.repository.UserTermsAgreementRepository;
import com.vitacheck.user.dto.UserSignedUpEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventService {

    private final TermsService termsService;
    private final NotificationSettingsService notificationSettingsService;
    private final TermsRepository termsRepository;
    private final UserTermsAgreementRepository userTermsAgreementRepository;

    @EventListener
    @Transactional
    public void handleUserSignedUpEvent(UserSignedUpEvent event) {
        if (event.getAgreeTermIds() != null && !event.getAgreeTermIds().isEmpty()) {
            List<Terms> termsList = termsRepository.findAllById(event.getAgreeTermIds());

            // ✅ 이 부분을 빌더 패턴으로 수정합니다.
            List<UserTermsAgreement> agreements = termsList.stream()
                    .map(terms -> UserTermsAgreement.builder()
                            .user(event.getUser())
                            .terms(terms)
                            .build())
                    .collect(Collectors.toList());

            userTermsAgreementRepository.saveAll(agreements);
        }
    }
}
