package com.vitacheck.domain.searchLog;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSearchLog is a Querydsl query type for SearchLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSearchLog extends EntityPathBase<SearchLog> {

    private static final long serialVersionUID = 1133666963L;

    public static final QSearchLog searchLog = new QSearchLog("searchLog");

    public final com.vitacheck.common.entity.QBaseTimeEntity _super = new com.vitacheck.common.entity.QBaseTimeEntity(this);

    public final NumberPath<Integer> age = createNumber("age", Integer.class);

    public final EnumPath<SearchCategory> category = createEnum("category", SearchCategory.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<com.vitacheck.common.enums.Gender> gender = createEnum("gender", com.vitacheck.common.enums.Gender.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath keyword = createString("keyword");

    public final EnumPath<Method> method = createEnum("method", Method.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QSearchLog(String variable) {
        super(SearchLog.class, forVariable(variable));
    }

    public QSearchLog(Path<? extends SearchLog> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSearchLog(PathMetadata metadata) {
        super(SearchLog.class, metadata);
    }

}

