package com.vitacheck.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSupplementLike is a Querydsl query type for SupplementLike
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSupplementLike extends EntityPathBase<SupplementLike> {

    private static final long serialVersionUID = -1565156025L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSupplementLike supplementLike = new QSupplementLike("supplementLike");

    public final com.vitacheck.common.entity.QBaseTimeEntity _super = new com.vitacheck.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QSupplement supplement;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.vitacheck.user.domain.QUser user;

    public QSupplementLike(String variable) {
        this(SupplementLike.class, forVariable(variable), INITS);
    }

    public QSupplementLike(Path<? extends SupplementLike> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSupplementLike(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSupplementLike(PathMetadata metadata, PathInits inits) {
        this(SupplementLike.class, metadata, inits);
    }

    public QSupplementLike(Class<? extends SupplementLike> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.supplement = inits.isInitialized("supplement") ? new QSupplement(forProperty("supplement"), inits.get("supplement")) : null;
        this.user = inits.isInitialized("user") ? new com.vitacheck.user.domain.QUser(forProperty("user")) : null;
    }

}

