package com.vitacheck.service;

import com.vitacheck.domain.purposes.AllPurpose;
import com.vitacheck.dto.AllPurposeDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurposeService {

    public List<AllPurposeDto> getAllPurposes() {
        return List.of(AllPurpose.values()).stream()
                .map(purpose -> new AllPurposeDto(purpose.name(), purpose.getDescription()))
                .collect(Collectors.toList());
    }
}
