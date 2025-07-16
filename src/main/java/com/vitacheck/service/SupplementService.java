package com.vitacheck.service;

import com.vitacheck.domain.Supplement;
import com.vitacheck.dto.SupplementDto;
import com.vitacheck.repository.SupplementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplementService {

    private final SupplementRepository supplementRepository;

    public List<SupplementDto.SearchResponse> search(String keyword, String brandName, String ingredientName) {
        List<Supplement> supplements = supplementRepository.search(keyword, brandName, ingredientName);

        return supplements.stream()
                .map(SupplementDto.SearchResponse::from)
                .collect(Collectors.toList());
    }
}