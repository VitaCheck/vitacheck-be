package com.vitacheck.common.security;

import com.vitacheck.common.enums.Gender;
import java.time.LocalDate;

public interface UserContextProvider {
    Long getCurrentUserId();     // 회원 ID
    String getCurrentEmail();    // 이메일
    Gender getCurrentGender();   // 성별
    LocalDate getCurrentBirthDate(); // 생년월일
    Integer getCurrentAge(); // 생년월일
    boolean isAuthenticated();   // 로그인 여부
}

