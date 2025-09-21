package com.vitacheck.Term.repository;

import com.vitacheck.Term.domain.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsRepository extends JpaRepository<Terms, Long> {
}