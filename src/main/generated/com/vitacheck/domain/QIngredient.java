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

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> recommendedDosage = createNumber("recommendedDosage", Integer.class);

    public final ListPath<com.vitacheck.domain.mapping.SupplementIngredient, com.vitacheck.domain.mapping.QSupplementIngredient> supplementIngredients = this.<com.vitacheck.domain.mapping.SupplementIngredient, com.vitacheck.domain.mapping.QSupplementIngredient>createList("supplementIngredients", com.vitacheck.domain.mapping.SupplementIngredient.class, com.vitacheck.domain.mapping.QSupplementIngredient.class, PathInits.DIRECT2);

    public final StringPath unit = createString("unit");

    public final NumberPath<Integer> upperLimit = createNumber("upperLimit", Integer.class);

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

