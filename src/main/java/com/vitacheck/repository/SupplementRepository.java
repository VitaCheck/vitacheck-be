package com.vitacheck.repository;

import com.vitacheck.domain.Supplement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplementRepository extends JpaRepository<Supplement, Long>, SupplementRepositoryCustom {
    @Query("SELECT s FROM Supplement s JOIN FETCH s.supplementIngredients si JOIN FETCH si.ingredient WHERE s.id IN :ids")
    List<Supplement> findSupplementsWithIngredientsByIds(@Param("ids") List<Long> ids);

    @EntityGraph(attributePaths = {"brand", "supplementIngredients", "supplementIngredients.ingredient"})
    Optional<Supplement> findById(Long id);

    List<Supplement> findAllByBrandId(Long brandId);

    @Query("""
           SELECT s FROM Supplement s JOIN FETCH s.supplementIngredients si
           JOIN FETCH si.ingredient i WHERE s.id = :id
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

}