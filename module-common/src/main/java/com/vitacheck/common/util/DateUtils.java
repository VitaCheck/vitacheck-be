package com.vitacheck.common.util;


import java.time.LocalDate;
import java.time.Period;

public class DateUtils {

    /**
     * LocalDate 객체를 기반으로 연령대를 계산합니다.
     * @param birthDate LocalDate 타입의 생년월일
     * @return "20대", "30대" 등 형식의 연령대 문자열
     */
    public static String calculateAgeGroup(LocalDate birthDate) { // ✅ 파라미터 타입 변경
        if (birthDate == null) {
            return "알 수 없음";
        }

        // ✅ 복잡한 문자열 파싱 및 2000년대 이전 처리 로직이 모두 필요 없어짐
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        int ageGroup = (age / 10) * 10;

        if (ageGroup >= 60) return "60대 이상";
        return ageGroup + "대";
    }
}