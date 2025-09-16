package com.vitacheck.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QIngredient is a Querydsl query type for Ingredient
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QIngredient extends EntityPathBase<Ingredient> {

    private static final long serialVersionUID = 1863277224L;

    public static final QIngredient ingredient = new QIngredient("ingredient");

    public final com.vitacheck.common.entity.QBaseTimeEntity _super = new com.vitacheck.common.entity.QBaseTimeEntity(this);

    public final ListPath<com.vitacheck.domain.mapping.IngredientAlternativeFood, com.vitacheck.domain.mapping.QIngredientAlternativeFood> alternativeFoods = this.<com.vitacheck.domain.mapping.IngredientAlternativeFood, com.vitacheck.domain.mapping.QIngredientAlternativeFood>createList("alternativeFoods", com.vitacheck.domain.mapping.IngredientAlternativeFood.class, com.vitacheck.domain.mapping.QIngredientAlternativeFood.class, PathInits.DIRECT2);

    public final StringPath caution = createString("caution");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final ListPath<IngredientDosage, QIngredientDosage> dosages = this.<IngredientDosage, QIngredientDosage>createList("dosages", IngredientDosage.class, QIngredientDosage.class, PathInits.DIRECT2);

    public final StringPath effect = createString("effect");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final ListPath<com.vitacheck.domain.purposes.PurposeIngredient, com.vitacheck.domain.purposes.QPurposeIngredient> purposeIngredients = this.<com.vitacheck.domain.purposes.PurposeIngredient, com.vitacheck.domain.purposes.QPurposeIngredient>createList("purposeIngredients", com.vitacheck.domain.purposes.PurposeIngredient.class, com.vitacheck.domain.purposes.QPurposeIngredient.class, PathInits.DIRECT2);

    public final SetPath<com.vitacheck.domain.mapping.SupplementIngredient, com.vitacheck.domain.mapping.QSupplementIngredient> supplementIngredients = this.<com.vitacheck.domain.mapping.SupplementIngredient, com.vitacheck.domain.mapping.QSupplementIngredient>createSet("supplementIngredients", com.vitacheck.domain.mapping.SupplementIngredient.class, com.vitacheck.domain.mapping.QSupplementIngredient.class, PathInits.DIRECT2);

    public final StringPath unit = createString("unit");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QIngredient(String variable) {
        super(Ingredient.class, forVariable(variable));
    }

    public QIngredient(Path<? extends Ingredient> path) {
        super(path.getType(), path.getMetadata());
    }

    public QIngredient(PathMetadata metadata) {
        super(Ingredient.class, metadata);
    }

}

