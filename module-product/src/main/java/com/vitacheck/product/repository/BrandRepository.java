package com.vitacheck.product.repository;

import com.vitacheck.product.domain.Supplement.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    String existsByName(String name);
    List<Brand> findByNameContainingIgnoreCase(String keyword);
}