package com.vitacheck.repository;

import com.vitacheck.domain.searchLog.SearchLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long>, SearchLogRepositoryCustom {


    @Query("SELECT s.keyword FROM SearchLog s " +
            "WHERE s.userId = :userId " +
            "AND s.method = com.vitacheck.domain.searchLog.Method.SEARCH " +
            "GROUP BY s.keyword " +
            "ORDER BY MAX(s.createdAt) DESC")
    List<String> findRecentKeywordsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT s.keyword FROM SearchLog s " +
            "WHERE s.userId = :userId " +
            "AND s.method = com.vitacheck.domain.searchLog.Method.CLICK " +
            "AND s.category = com.vitacheck.domain.searchLog.SearchCategory.SUPPLEMENT " +
            "GROUP BY s.keyword " +
            "ORDER BY MAX(s.createdAt) DESC")
    List<String> findRecentViewedSupplementNamesByUserId(@Param("userId") Long userId, Pageable pageable);

    void deleteAllByUserId(Long userId);
}
