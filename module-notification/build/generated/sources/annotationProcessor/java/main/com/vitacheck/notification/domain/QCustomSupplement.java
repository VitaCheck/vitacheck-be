package com.vitacheck.notification.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCustomSupplement is a Querydsl query type for CustomSupplement
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCustomSupplement extends EntityPathBase<CustomSupplement> {

    private static final long serialVersionUID = -340986716L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCustomSupplement customSupplement = new QCustomSupplement("customSupplement");

    public final com.vitacheck.common.entity.QBaseTimeEntity _super = new com.vitacheck.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final StringPath name = createString("name");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.vitacheck.user.domain.QUser user;

    public QCustomSupplement(String variable) {
        this(CustomSupplement.class, forVariable(variable), INITS);
    }

    public QCustomSupplement(Path<? extends CustomSupplement> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCustomSupplement(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCustomSupplement(PathMetadata metadata, PathInits inits) {
        this(CustomSupplement.class, metadata, inits);
    }

    public QCustomSupplement(Class<? extends CustomSupplement> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.vitacheck.user.domain.QUser(forProperty("user")) : null;
    }

}

