package com.vitacheck.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QIngredientLike is a Querydsl query type for IngredientLike
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QIngredientLike extends EntityPathBase<IngredientLike> {

    private static final long serialVersionUID = 1295379039L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QIngredientLike ingredientLike = new QIngredientLike("ingredientLike");

    public final com.vitacheck.common.entity.QBaseTimeEntity _super = new com.vitacheck.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QIngredient ingredient;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.vitacheck.user.domain.QUser user;

    public QIngredientLike(String variable) {
        this(IngredientLike.class, forVariable(variable), INITS);
    }

    public QIngredientLike(Path<? extends IngredientLike> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QIngredientLike(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QIngredientLike(PathMetadata metadata, PathInits inits) {
        this(IngredientLike.class, metadata, inits);
    }

    public QIngredientLike(Class<? extends IngredientLike> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.ingredient = inits.isInitialized("ingredient") ? new QIngredient(forProperty("ingredient")) : null;
        this.user = inits.isInitialized("user") ? new com.vitacheck.user.domain.QUser(forProperty("user")) : null;
    }

}

