package com.vitacheck.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLike is a Querydsl query type for Like
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLike extends EntityPathBase<Like> {

    private static final long serialVersionUID = 1514718638L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLike like = new QLike("like1");

    public final com.vitacheck.domain.common.QBaseTimeEntity _super = new com.vitacheck.domain.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QSupplement supplement;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.vitacheck.domain.user.QUser user;

    public QLike(String variable) {
        this(Like.class, forVariable(variable), INITS);
    }

    public QLike(Path<? extends Like> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLike(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLike(PathMetadata metadata, PathInits inits) {
        this(Like.class, metadata, inits);
    }

    public QLike(Class<? extends Like> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.supplement = inits.isInitialized("supplement") ? new QSupplement(forProperty("supplement"), inits.get("supplement")) : null;
        this.user = inits.isInitialized("user") ? new com.vitacheck.domain.user.QUser(forProperty("user")) : null;
    }

}

