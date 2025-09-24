package com.vitacheck.domain.mapping;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QIngredientAlternativeFood is a Querydsl query type for IngredientAlternativeFood
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QIngredientAlternativeFood extends EntityPathBase<IngredientAlternativeFood> {

    private static final long serialVersionUID = -330773565L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QIngredientAlternativeFood ingredientAlternativeFood = new QIngredientAlternativeFood("ingredientAlternativeFood");

    public final com.vitacheck.common.entity.QBaseTimeEntity _super = new com.vitacheck.common.entity.QBaseTimeEntity(this);

    public final com.vitacheck.domain.QAlternativeFood alternativeFood;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.vitacheck.domain.QIngredient ingredient;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QIngredientAlternativeFood(String variable) {
        this(IngredientAlternativeFood.class, forVariable(variable), INITS);
    }

    public QIngredientAlternativeFood(Path<? extends IngredientAlternativeFood> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QIngredientAlternativeFood(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QIngredientAlternativeFood(PathMetadata metadata, PathInits inits) {
        this(IngredientAlternativeFood.class, metadata, inits);
    }

    public QIngredientAlternativeFood(Class<? extends IngredientAlternativeFood> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.alternativeFood = inits.isInitialized("alternativeFood") ? new com.vitacheck.domain.QAlternativeFood(forProperty("alternativeFood")) : null;
        this.ingredient = inits.isInitialized("ingredient") ? new com.vitacheck.domain.QIngredient(forProperty("ingredient")) : null;
    }

}

