package com.vitacheck.etc;

import com.vitacheck.user.service.NotificationSettingsService;
import com.vitacheck.Term.service.TermsService;
import com.vitacheck.Term.domain.Terms;
import com.vitacheck.Term.domain.UserTermsAgreement;
import com.vitacheck.Term.repository.TermsRepository;
import com.vitacheck.Term.repository.UserTermsAgreementRepository;
import com.vitacheck.user.dto.UserSignedUpEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

            List<UserTermsAgreement> agreements = termsList.stream()
                    .map(terms -> UserTermsAgreement.builder()
                            .user(event.getUser())
                            .terms(terms)
                            .build())
                    .collect(Collectors.toList());

            userTermsAgreementRepository.saveAll(agreements);
        }

        notificationSettingsService.createDefaultSettingsForUser(event.getUser());
        log.info("사용자 ID: {}의 기본 알림 설정을 생성했습니다.", event.getUser().getId());
    }
}
