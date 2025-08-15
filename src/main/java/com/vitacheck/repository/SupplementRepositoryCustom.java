package com.vitacheck.repository;

import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.purposes.AllPurpose;
import com.vitacheck.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SupplementRepositoryCustom {
    Page<Supplement> search(User user, String keyword, String brandName, String ingredientName, Pageable pageable);
    Page<Supplement> findByPurposeNames(List<AllPurpose> purposeNames, Pageable pageable);
}