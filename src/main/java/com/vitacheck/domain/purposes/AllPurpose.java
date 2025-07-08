package com.vitacheck.domain.purposes;

import lombok.Getter;

@Getter
public enum AllPurpose {

    EYE("눈건강"),
    BONE("뼈건강"),
    SLEEP_STRESS("수면/스트레스"),
    CHOLESTEROL("혈중 콜레스테롤"),
    FAT("체지방"),
    SKIN("피부 건강"),
    TIRED("피로감"),
    IMMUNE("면역력"),
    DIGEST("소화/위 건강"),
    ATHELETIC("운동 능력"),
    CLIMACTERIC("여성 갱년기"),
    TEETH("치아/잇몸"),
    HAIR_NAIL("탈모/손톱 건강"),
    BLOOD_PRESS("혈압"),
    NEUTRAL_FAT("혈중 중성지방"),
    ANEMIA("빈혈"),
    ANTIAGING("노화/항산화"),
    BRAIN("두뇌활동"),
    LIVER("간 건강"),
    BLOOD_CIRCULATION("혈관/혈액 순환"),
    GUT_HEALTH("장 건강"),
    RESPIRATORY_HEALTH("호흡기 건강"),
    JOINT_HEALTH("관절 건강"),
    PREGNANT_HEALTH("임산부/태아 건강"),
    BLOOD_SUGAR("혈당"),
    THYROID_HEALTH("갑상선 건강"),
    WOMAN_HEALTH("여성 건강"),
    MAN_HEALTH("남성 건강");

    private final String description;

    AllPurpose(String description) {
        this.description = description;
    }
}

