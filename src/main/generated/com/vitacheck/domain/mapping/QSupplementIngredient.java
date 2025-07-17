package com.vitacheck.domain.mapping;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSupplementIngredient is a Querydsl query type for SupplementIngredient
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSupplementIngredient extends EntityPathBase<SupplementIngredient> {

    private static final long serialVersionUID = 344434081L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSupplementIngredient supplementIngredient = new QSupplementIngredient("supplementIngredient");

    public final NumberPath<Integer> amount = createNumber("amount", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.vitacheck.domain.QIngredient ingredient;

    public final com.vitacheck.domain.QSupplement supplement;

    public final StringPath unit = createString("unit");

    public QSupplementIngredient(String variable) {
        this(SupplementIngredient.class, forVariable(variable), INITS);
    }

    public QSupplementIngredient(Path<? extends SupplementIngredient> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSupplementIngredient(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSupplementIngredient(PathMetadata metadata, PathInits inits) {
        this(SupplementIngredient.class, metadata, inits);
    }

    public QSupplementIngredient(Class<? extends SupplementIngredient> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.ingredient = inits.isInitialized("ingredient") ? new com.vitacheck.domain.QIngredient(forProperty("ingredient")) : null;
        this.supplement = inits.isInitialized("supplement") ? new com.vitacheck.domain.QSupplement(forProperty("supplement"), inits.get("supplement")) : null;
    }

}

