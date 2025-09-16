package com.vitacheck.repository;

import com.vitacheck.domain.Supplement;
import com.vitacheck.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SupplementRepositoryCustom {
    Page<Supplement> search(User user, String keyword, String brandName, String ingredientName, Pageable pageable);
}