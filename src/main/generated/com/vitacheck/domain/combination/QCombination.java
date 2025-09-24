package com.vitacheck.domain.combination;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCombination is a Querydsl query type for Combination
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCombination extends EntityPathBase<Combination> {

    private static final long serialVersionUID = 1538566841L;

    public static final QCombination combination = new QCombination("combination");

    public final com.vitacheck.common.entity.QBaseTimeEntity _super = new com.vitacheck.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Integer> displayRank = createNumber("displayRank", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<com.vitacheck.domain.Ingredient, com.vitacheck.domain.QIngredient> ingredients = this.<com.vitacheck.domain.Ingredient, com.vitacheck.domain.QIngredient>createList("ingredients", com.vitacheck.domain.Ingredient.class, com.vitacheck.domain.QIngredient.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    public final EnumPath<RecommandType> type = createEnum("type", RecommandType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCombination(String variable) {
        super(Combination.class, forVariable(variable));
    }

    public QCombination(Path<? extends Combination> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCombination(PathMetadata metadata) {
        super(Combination.class, metadata);
    }

}

