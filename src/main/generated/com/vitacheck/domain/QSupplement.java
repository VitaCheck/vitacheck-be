package com.vitacheck.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSupplement is a Querydsl query type for Supplement
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSupplement extends EntityPathBase<Supplement> {

    private static final long serialVersionUID = 1357693328L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSupplement supplement = new QSupplement("supplement");

    public final QBaseTimeEntity _super = new QBaseTimeEntity(this);

    public final QBrand brand;

    public final StringPath caution = createString("caution");

    public final StringPath coupangUrl = createString("coupangUrl");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final StringPath method = createString("method");

    public final StringPath name = createString("name");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final ListPath<SupplementIngredient, QSupplementIngredient> supplementIngredients = this.<SupplementIngredient, QSupplementIngredient>createList("supplementIngredients", SupplementIngredient.class, QSupplementIngredient.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSupplement(String variable) {
        this(Supplement.class, forVariable(variable), INITS);
    }

    public QSupplement(Path<? extends Supplement> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSupplement(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSupplement(PathMetadata metadata, PathInits inits) {
        this(Supplement.class, metadata, inits);
    }

    public QSupplement(Class<? extends Supplement> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.brand = inits.isInitialized("brand") ? new QBrand(forProperty("brand")) : null;
    }

}

