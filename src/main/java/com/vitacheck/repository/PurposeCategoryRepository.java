package com.vitacheck.repository;

import com.vitacheck.domain.purposes.PurposeCategory;
import com.vitacheck.domain.purposes.AllPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurposeCategoryRepository extends JpaRepository<PurposeCategory, Long> {
    Optional<PurposeCategory> findByName(AllPurpose name);
    List<PurposeCategory> findAllByNameIn(List<AllPurpose> names);
}
