package com.vitacheck.repository;

import com.vitacheck.domain.purposes.AllPurpose;
import org.springframework.data.domain.Page;
import com.vitacheck.dto.PurposeIngredientSupplementRow;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PurposeQueryRepository {
    Page<PurposeIngredientSupplementRow> findByPurposes(List<AllPurpose> purposes, Pageable pageable);

    Page<PurposeIngredientSupplementRow> findByPurposesPagedByIngredient(List<AllPurpose> purposes, Pageable pageable);

}
