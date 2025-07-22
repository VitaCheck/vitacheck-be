package com.vitacheck.repository;

import com.vitacheck.domain.purposes.AllPurpose;
import com.vitacheck.domain.purposes.PurposeCategory;

import java.util.List;

public interface PurposeCategoryQueryRepository {
    List<PurposeCategory> findAllWithIngredientAndSupplementByNameIn(List<AllPurpose> names);
}
