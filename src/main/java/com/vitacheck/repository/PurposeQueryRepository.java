//package com.vitacheck.repository;
//
//import com.vitacheck.domain.purposes.AllPurpose;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Slice;
//
//import javax.annotation.Nullable;
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//
//public interface PurposeQueryRepository {
//
//    /**
//     * 선택된 목적(ENUM) 조건을 만족하고 최소 1개 이상의 보충제와 연결된
//     * 영양소(ingredient)들 중에서, 성분 ID만 얇게 페이지네이션하여 반환
//     * 정렬 기준: ingredient.name ASC, ingredient.id ASC
//     */
//    Slice<Long> findIngredientIdPageByPurposes(List<AllPurpose> purposes, Pageable pageable);
//
//    /**
//     * 주어진 성분 ID 집합에 대해, (옵션) 목적 필터를 적용하여
//     * 각 성분이 가지는 목적 ENUM 리스트를 반환
//     */
//    Map<Long, List<AllPurpose>> findPurposesByIngredientIds(
//            Collection<Long> ingredientIds,
//            @Nullable List<AllPurpose> filterPurposes
//    );
//
//    /**
//     * 주어진 성분 ID 집합에 대해, 각 성분에 연결된 보충제 요약 목록을 반환
//     * (목적 조인 없이 성분-보충제 매핑만)
//     */
////    Map<Long, List<PurposeQueryRepositoryImpl.SupplementBriefRow>> findSupplementsByIngredientIds(
////            Collection<Long> ingredientIds
////    );
//
//    /**
//     * 주어진 성분 ID 집합에 대해, 성분 ID 로 성분명을 매핑해 반환
//     */
//    Map<Long, String> findIngredientNames(Collection<Long> ingredientIds);
//}
