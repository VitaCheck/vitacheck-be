package com.vitacheck.domain.purposes;

import com.vitacheck.domain.Ingredient;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "purpose_ingredient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@IdClass(PurposeIngredient.PurposeIngredientId.class)
public class PurposeIngredient {

    // ✅ 2. @Id 어노테이션을 두 개의 키 필드에 모두 붙입니다.
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purpose_id")
    private PurposeCategory purposeCategory;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    // ✅ 3. 복합 키를 표현하는 내부 클래스를 추가합니다.
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurposeIngredientId implements Serializable {
        private Long purposeCategory;
        private Long ingredient;
    }

    // ❌ 기존의 Long id 필드는 삭제합니다.
}