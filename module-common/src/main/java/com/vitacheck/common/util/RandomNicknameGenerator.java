package com.vitacheck.common.util;

import java.util.List;
import java.util.Random;

public class RandomNicknameGenerator {

    private static final List<String> ADJECTIVES = List.of(
            "행복한", "슬픈", "즐거운", "화나는", "수영하는", "달리는", "잠자는",
            "용감한", "수줍은", "똑똑한", "친절한", "배고픈", "하품하는"
    );

    private static final List<String> NOUNS = List.of(
            "강아지", "고양이", "호랑이", "사자", "코끼리", "기린", "원숭이",
            "판다", "두더지", "쿼카", "알파카", "카피바라", "다람쥐"
    );

    private static final Random RANDOM = new Random();

    public static String generate() {
        // 형용사 중 하나를 선택
        String adjective = ADJECTIVES.get(RANDOM.nextInt(ADJECTIVES.size()));
        // 명사 중 하나를 선택
        String noun = NOUNS.get(RANDOM.nextInt(NOUNS.size()));

        return adjective + " " + noun;
    }
}
