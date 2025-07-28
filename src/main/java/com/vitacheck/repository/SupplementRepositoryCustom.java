package com.vitacheck.repository;

import com.vitacheck.domain.Supplement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SupplementRepositoryCustom {
    Page<Supplement> search(String keyword, String brandName, String ingredientName, Pageable pageable);
}