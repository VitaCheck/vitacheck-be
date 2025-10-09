package com.vitacheck.product.repository;

import com.vitacheck.product.domain.Supplement.Supplement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface SupplementRepository extends JpaRepository<Supplement, Long>{
//    Collection<Object> findAllByBrandId(Long brandId);
    List<Supplement> findAllByBrandId(Long brandId);

    @Query("SELECT s FROM Supplement s JOIN FETCH s.supplementIngredients si JOIN FETCH si.ingredient WHERE s.id IN :ids")
    List<Supplement> findSupplementsWithIngredientsByIds(@Param("ids") List<Long> ids);

    @EntityGraph(attributePaths = {"brand", "supplementIngredients", "supplementIngredients.ingredient"})
    Optional<Supplement> findById(Long id);

//    List<Supplement> findAllByBrandId(Long brandId);

    @Query("""
           SELECT s FROM Supplement s JOIN FETCH s.supplementIngredients si
           JOIN FETCH si.ingredient i WHERE s.id = :
                      id
           """)
    Optional<Supplement> findByIdWithIngredients(@Param("id") Long id);

    String existsByName(String name);

    List<Supplement> findAllByNameIn(List<String> names);

    @Query("""
    SELECT s
    FROM Supplement s
    JOIN FETCH s.brand b
    LEFT JOIN FETCH s.supplementIngredients si
    LEFT JOIN FETCH si.ingredient i
    WHERE s.id = :id
""")
    Optional<Supplement> findByIdWithBrandAndIngredients(@Param("id") Long id);


    @Query(
            value = """
                WITH matched_supplements AS (
                    -- 1) 성분명 매칭
                    SELECT DISTINCT si.supplement_id AS id
                    FROM ingredients i
                    JOIN supplement_ingredients si ON si.ingredient_id = i.id
                    WHERE i.name LIKE CONCAT('%', :keyword, '%')
                
                    UNION
                
                    -- 2) 브랜드명 매칭
                    SELECT DISTINCT s.id
                    FROM brands b
                    JOIN supplements s ON s.brand_id = b.id
                    WHERE b.name LIKE CONCAT('%', :keyword, '%')
                
                    UNION
                
                    -- 3) 제품명 매칭
                    SELECT DISTINCT s.id
                    FROM supplements s
                    WHERE s.name LIKE CONCAT('%', :keyword, '%')
                ),
                log_counts AS (
                    SELECT sl.keyword AS supplement_name,
                           COUNT(*) AS popularity_count
                    FROM search_logs sl
                    WHERE sl.category = 'SUPPLEMENT'
                      AND sl.created_at >= CURDATE() - INTERVAL 3 DAY
                    GROUP BY sl.keyword
                )
                        
                SELECT s.id AS supplementId,
                       s.name AS supplementName,
                       s.coupang_url AS coupangUrl,
                       s.image_url AS imageUrl,
                       COALESCE(lc.popularity_count, 0) AS popularity,
                       (COALESCE(lc.popularity_count, 0) * 1000000 + s.id) AS cursorId
                FROM supplements s
                JOIN matched_supplements ms ON ms.id = s.id
                LEFT JOIN log_counts lc ON lc.supplement_name = s.name
                WHERE (:cursorId IS NULL OR (COALESCE(lc.popularity_count, 0) * 1000000 + s.id) < :cursorId)
                ORDER BY cursorId DESC
                LIMIT :limit;

        """,
            nativeQuery = true
    )
    List<Object[]> findSupplementsByKeywordWithPopularity(
            @Param("keyword") String keyword,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit

    );

    // 목적(purpose_ids)에 맞는 성분을 가진 영양제 후보 Top-N을 뽑아온다.
    // 1) 목적 커버리지(서로 다른 purpose_id 매칭 수)
    // 2) 매칭 성분 수
    // 3) 재고/인기 등 추가 정렬 기준은 나중에 컬럼 생기면 추가
    @Query(
            value = """
        SELECT s.id
        FROM supplements s
        JOIN supplement_ingredients si ON si.supplement_id = s.id
        JOIN purpose_ingredients   pi ON pi.ingredient_id   = si.ingredient_id
        WHERE pi.purpose_id IN (:purposeIds)
        GROUP BY s.id
        ORDER BY COUNT(DISTINCT pi.purpose_id) DESC,
                 COUNT(*) DESC,
                 s.id ASC
        LIMIT :limit
        """,
            nativeQuery = true
    )
    List<Long> findTopSupplementIdsForPurposes(List<Long> purposeIds, int limit);
}
