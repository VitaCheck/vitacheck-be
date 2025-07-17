package com.vitacheck.domain.combination;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCombination is a Querydsl query type for Combination
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCombination extends EntityPathBase<Combination> {

    private static final long serialVersionUID = 1538566841L;

    public static final QCombination combination = new QCombination("combination");

    public final StringPath description = createString("description");

    public final NumberPath<Integer> displayRank = createNumber("displayRank", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final EnumPath<RecommandType> type = createEnum("type", RecommandType.class);

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

