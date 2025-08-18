package com.vitacheck.dto;

import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
public class SliceResponseDto<T> {
    private final List<T> content;
    private final boolean hasNext;

    public SliceResponseDto(Slice<T> slice) {
        this.content = slice.getContent();
        this.hasNext = slice.hasNext();
    }
}