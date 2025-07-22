package com.vitacheck.domain.mapping;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QIngredientCategory is a Querydsl query type for IngredientCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QIngredientCategory extends EntityPathBase<IngredientCategory> {

    private static final long serialVersionUID = 1494656486L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QIngredientCategory ingredientCategory = new QIngredientCategory("ingredientCategory");

    public final com.vitacheck.domain.purposes.QPurposeCategory category;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.vitacheck.domain.QIngredient ingredient;

    public QIngredientCategory(String variable) {
        this(IngredientCategory.class, forVariable(variable), INITS);
    }

    public QIngredientCategory(Path<? extends IngredientCategory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QIngredientCategory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QIngredientCategory(PathMetadata metadata, PathInits inits) {
        this(IngredientCategory.class, metadata, inits);
    }

    public QIngredientCategory(Class<? extends IngredientCategory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new com.vitacheck.domain.purposes.QPurposeCategory(forProperty("category")) : null;
        this.ingredient = inits.isInitialized("ingredient") ? new com.vitacheck.domain.QIngredient(forProperty("ingredient")) : null;
    }

}

