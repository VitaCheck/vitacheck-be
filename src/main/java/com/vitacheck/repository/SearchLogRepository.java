package com.vitacheck.repository;

import com.vitacheck.domain.searchLog.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {
}
