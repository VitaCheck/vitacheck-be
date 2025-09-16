package com.vitacheck.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRoutineDetail is a Querydsl query type for RoutineDetail
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoutineDetail extends EntityPathBase<RoutineDetail> {

    private static final long serialVersionUID = -157313538L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRoutineDetail routineDetail = new QRoutineDetail("routineDetail");

    public final com.vitacheck.common.entity.QBaseTimeEntity _super = new com.vitacheck.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<RoutineDayOfWeek> dayOfWeek = createEnum("dayOfWeek", RoutineDayOfWeek.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.vitacheck.domain.notification.QNotificationRoutine notificationRoutine;

    public final TimePath<java.time.LocalTime> time = createTime("time", java.time.LocalTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QRoutineDetail(String variable) {
        this(RoutineDetail.class, forVariable(variable), INITS);
    }

    public QRoutineDetail(Path<? extends RoutineDetail> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRoutineDetail(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRoutineDetail(PathMetadata metadata, PathInits inits) {
        this(RoutineDetail.class, metadata, inits);
    }

    public QRoutineDetail(Class<? extends RoutineDetail> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.notificationRoutine = inits.isInitialized("notificationRoutine") ? new com.vitacheck.domain.notification.QNotificationRoutine(forProperty("notificationRoutine"), inits.get("notificationRoutine")) : null;
    }

}

