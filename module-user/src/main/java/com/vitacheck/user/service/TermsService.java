package com.vitacheck.user.service;

import com.vitacheck.common.event.MarketingTermsAgreedEvent;
import com.vitacheck.common.event.MarketingTermsWithdrawEvent;
import com.vitacheck.user.domain.UserTermsAgreement;
import com.vitacheck.user.domain.Terms;
import com.vitacheck.user.dto.TermsDto;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.user.repository.TermsRepository;
import com.vitacheck.user.repository.UserTermsAgreementRepository;
import com.vitacheck.user.domain.User;
import com.vitacheck.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {

    private final TermsRepository termsRepository;
    private final UserRepository userRepository;
    private final UserTermsAgreementRepository userTermsAgreementRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<TermsDto.TermResponse> getAllTerms() {
        return termsRepository.findAll().stream()
                .map(TermsDto.TermResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void agreeToTerms(Long userId, TermsDto.AgreementRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        // 중복 로직을 private 메서드로 호출
        processAgreements(user, request.getAgreedTermIds());
    }

    @Transactional
    public void agreeToTerms(User user, List<Long> agreedTermIds) {
        // 중복 로직을 private 메서드로 호출
        processAgreements(user, agreedTermIds);
    }

    private void processAgreements(User user, List<Long> agreedTermIds) {
        Set<Long> alreadyAgreedTermIds = userTermsAgreementRepository.findByUser(user).stream()
                .map(agreement -> agreement.getTerms().getId())
                .collect(Collectors.toSet());

        List<Long> newTermIdsToAgree = agreedTermIds.stream()
                .filter(id -> !alreadyAgreedTermIds.contains(id))
                .collect(Collectors.toList());

        if (newTermIdsToAgree.isEmpty()) {
            return;
        }

        List<Terms> termsToAgree = termsRepository.findAllById(newTermIdsToAgree);
        if (termsToAgree.size() != newTermIdsToAgree.size()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        boolean allRequiredAgreed = termsToAgree.stream()
                .filter(Terms::isRequired)
                .count() == termsRepository.findAll().stream().filter(Terms::isRequired).count();

        long requiredTermsCountInRequest = termsToAgree.stream().filter(Terms::isRequired).count();
        long totalRequiredTerms = termsRepository.findAll().stream().filter(Terms::isRequired).count();

        if (requiredTermsCountInRequest != totalRequiredTerms) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        boolean marketingAgreed = termsToAgree.stream()
                .anyMatch(term -> !term.isRequired() && "마케팅 목적의 개인정보 수집 및 이용에 대한 동의".equals(term.getTitle()));

        if (marketingAgreed) {
            eventPublisher.publishEvent(new MarketingTermsAgreedEvent(user.getId()));
        }

        List<UserTermsAgreement> agreements = termsToAgree.stream()
                .map(term -> UserTermsAgreement.builder().user(user).terms(term).build())
                .collect(Collectors.toList());

        userTermsAgreementRepository.saveAll(agreements);
    }

    @Transactional
    public void withdrawTerms(Long userId, TermsDto.AgreementWithdrawalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Long> termIdsToWithdraw = request.getWithdrawnTermIds();
        List<Terms> termsToWithdraw = termsRepository.findAllById(termIdsToWithdraw);

        // 1. 마케팅 약관을 철회하는지 확인하고, 알림 설정 OFF
        boolean marketingWithdrawn = termsToWithdraw.stream()
                .anyMatch(term -> "마케팅 목적의 개인정보 수집 및 이용에 대한 동의".equals(term.getTitle()));

        if (marketingWithdrawn) {
            eventPublisher.publishEvent(new MarketingTermsWithdrawEvent(user.getId()));
        }

        // 2. DB에서 동의 내역 삭제
        userTermsAgreementRepository.deleteByUserAndTermsIdIn(user, termIdsToWithdraw);
    }
}