package com.vitacheck.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QIngredientDosage is a Querydsl query type for IngredientDosage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QIngredientDosage extends EntityPathBase<IngredientDosage> {

    private static final long serialVersionUID = -904513665L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QIngredientDosage ingredientDosage = new QIngredientDosage("ingredientDosage");

    public final com.vitacheck.common.entity.QBaseTimeEntity _super = new com.vitacheck.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<com.vitacheck.common.enums.Gender> gender = createEnum("gender", com.vitacheck.common.enums.Gender.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QIngredient ingredient;

    public final NumberPath<Integer> maxAge = createNumber("maxAge", Integer.class);

    public final NumberPath<Integer> minAge = createNumber("minAge", Integer.class);

    public final NumberPath<Double> recommendedDosage = createNumber("recommendedDosage", Double.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Double> upperLimit = createNumber("upperLimit", Double.class);

    public QIngredientDosage(String variable) {
        this(IngredientDosage.class, forVariable(variable), INITS);
    }

    public QIngredientDosage(Path<? extends IngredientDosage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QIngredientDosage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QIngredientDosage(PathMetadata metadata, PathInits inits) {
        this(IngredientDosage.class, metadata, inits);
    }

    public QIngredientDosage(Class<? extends IngredientDosage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.ingredient = inits.isInitialized("ingredient") ? new QIngredient(forProperty("ingredient")) : null;
    }

}

