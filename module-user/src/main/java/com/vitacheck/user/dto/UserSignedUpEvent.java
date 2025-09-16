package com.vitacheck.user.dto;

import com.vitacheck.user.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class UserSignedUpEvent {

    private final User user;
    private final List<Long> agreeTermIds;
}
