package com.vitacheck.product.repository;

import com.vitacheck.product.domain.Supplement.Supplement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplementRepository extends JpaRepository<Supplement, Long> {


}
