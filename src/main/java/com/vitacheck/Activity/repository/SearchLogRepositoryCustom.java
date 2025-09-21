package com.vitacheck.Activity.repository;

import com.querydsl.core.Tuple;
import com.vitacheck.common.enums.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchLogRepositoryCustom {
    List<Tuple> findPopularIngredientsByAgeGroup(Integer startAge, Integer endAge, int limit);
    Page<Tuple> findPopularSupplements(Integer startAge, Integer endAge, Gender gender, Pageable pageable);

}
