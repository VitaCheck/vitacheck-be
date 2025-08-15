package com.vitacheck.repository;

import com.vitacheck.domain.Ingredient;
import com.vitacheck.domain.purposes.AllPurpose;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IngredientRepositoryCustom {
    Page<Ingredient> findByPurposeNames(List<AllPurpose> purposeNames, Pageable pageable);
}
