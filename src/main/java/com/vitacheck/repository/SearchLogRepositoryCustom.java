package com.vitacheck.repository;

import com.querydsl.core.Tuple;
import com.vitacheck.dto.PopularIngredientDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchLogRepositoryCustom {
    List<Tuple> findPopularIngredients(int limit);
    Page<Tuple> findPopularSupplements(Integer startAge, Integer endAge, Pageable pageable);

}
