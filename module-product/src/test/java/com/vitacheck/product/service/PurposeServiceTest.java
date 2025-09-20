package com.vitacheck.product.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vitacheck.product.domain.Purpose.Purpose;
import com.vitacheck.product.repository.PurposeRepository;
import com.vitacheck.product.dto.PurposeResponseDTO;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;


class PurposeServiceTest {
    private static final Logger log = LoggerFactory.getLogger(PurposeServiceTest.class);

    // 모든 목적 반환 test
    @Test
    void getAllPurposes() {
        // given: 가짜 PurposeRepository 생성
        PurposeRepository mockRepo = Mockito.mock(PurposeRepository.class);
        JPAQueryFactory mockQueryFactory = Mockito.mock(JPAQueryFactory.class);

        PurposeService purposeService = new PurposeService(mockRepo, mockQueryFactory);

        // Purpose 엔티티 임의 데이터
        Purpose p1 = Purpose.builder().id(1L).name("눈 건강").build();
        Purpose p2 = Purpose.builder().id(2L).name("피로 개선").build();

        Mockito.when(mockRepo.findAll()).thenReturn(List.of(p1, p2));

        // when: 서비스 호출
        List<PurposeResponseDTO.AllPurposeDTO> result = purposeService.getAllPurposes();

        // then: 검증
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("눈 건강");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("피로 개선");

        // 결과 출력
        log.info("result = {}", result);

    }

}