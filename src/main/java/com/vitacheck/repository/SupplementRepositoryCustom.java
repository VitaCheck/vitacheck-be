package com.vitacheck.repository;

import com.vitacheck.domain.Supplement;
import java.util.List;

public interface SupplementRepositoryCustom {
    List<Supplement> search(String keyword, String brandName, String ingredientName);
}