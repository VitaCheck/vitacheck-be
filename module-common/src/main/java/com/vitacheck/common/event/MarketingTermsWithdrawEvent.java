package com.vitacheck.common.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MarketingTermsWithdrawEvent {
    private final Long userId;
}
