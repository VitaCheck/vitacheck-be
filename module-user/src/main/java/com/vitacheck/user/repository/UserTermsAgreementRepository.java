package com.vitacheck.user.repository;

import com.vitacheck.user.domain.UserTermsAgreement;
import com.vitacheck.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTermsAgreementRepository extends JpaRepository<UserTermsAgreement, Long> {
    // 사용자가 동의한 모든 약관 정보를 조회
    List<UserTermsAgreement> findByUser(User user);

    // ▼ [신규 추가] 사용자와 약관 ID 목록으로 동의 내역을 삭제하는 메서드 ▼
    void deleteByUserAndTermsIdIn(User user, List<Long> termIds);

    void deleteAllByUser(User user);
}
