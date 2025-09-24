package com.vitacheck.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAlternativeFood is a Querydsl query type for AlternativeFood
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAlternativeFood extends EntityPathBase<AlternativeFood> {

    private static final long serialVersionUID = 173477684L;

    public static final QAlternativeFood alternativeFood = new QAlternativeFood("alternativeFood");

    public final com.vitacheck.common.entity.QBaseTimeEntity _super = new com.vitacheck.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath emoji = createString("emoji");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<com.vitacheck.domain.mapping.IngredientAlternativeFood, com.vitacheck.domain.mapping.QIngredientAlternativeFood> ingredients = this.<com.vitacheck.domain.mapping.IngredientAlternativeFood, com.vitacheck.domain.mapping.QIngredientAlternativeFood>createList("ingredients", com.vitacheck.domain.mapping.IngredientAlternativeFood.class, com.vitacheck.domain.mapping.QIngredientAlternativeFood.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QAlternativeFood(String variable) {
        super(AlternativeFood.class, forVariable(variable));
    }

    public QAlternativeFood(Path<? extends AlternativeFood> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAlternativeFood(PathMetadata metadata) {
        super(AlternativeFood.class, metadata);
    }

}

