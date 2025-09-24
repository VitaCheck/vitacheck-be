package com.vitacheck.domain.purposes;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPurposeIngredient is a Querydsl query type for PurposeIngredient
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPurposeIngredient extends EntityPathBase<PurposeIngredient> {

    private static final long serialVersionUID = 1427278221L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPurposeIngredient purposeIngredient = new QPurposeIngredient("purposeIngredient");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.vitacheck.domain.QIngredient ingredient;

    public final QPurposeCategory purposeCategory;

    public QPurposeIngredient(String variable) {
        this(PurposeIngredient.class, forVariable(variable), INITS);
    }

    public QPurposeIngredient(Path<? extends PurposeIngredient> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPurposeIngredient(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPurposeIngredient(PathMetadata metadata, PathInits inits) {
        this(PurposeIngredient.class, metadata, inits);
    }

    public QPurposeIngredient(Class<? extends PurposeIngredient> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.ingredient = inits.isInitialized("ingredient") ? new com.vitacheck.domain.QIngredient(forProperty("ingredient")) : null;
        this.purposeCategory = inits.isInitialized("purposeCategory") ? new QPurposeCategory(forProperty("purposeCategory")) : null;
    }

}

