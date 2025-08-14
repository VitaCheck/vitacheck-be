package com.vitacheck.repository;

import com.vitacheck.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    String existsByName(String name);
}
