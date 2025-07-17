package com.vitacheck.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRoutineTime is a Querydsl query type for RoutineTime
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoutineTime extends EntityPathBase<RoutineTime> {

    private static final long serialVersionUID = 1931040698L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRoutineTime routineTime = new QRoutineTime("routineTime");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.vitacheck.domain.notification.QNotificationRoutine notificationRoutine;

    public final TimePath<java.time.LocalTime> time = createTime("time", java.time.LocalTime.class);

    public QRoutineTime(String variable) {
        this(RoutineTime.class, forVariable(variable), INITS);
    }

    public QRoutineTime(Path<? extends RoutineTime> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRoutineTime(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRoutineTime(PathMetadata metadata, PathInits inits) {
        this(RoutineTime.class, metadata, inits);
    }

    public QRoutineTime(Class<? extends RoutineTime> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.notificationRoutine = inits.isInitialized("notificationRoutine") ? new com.vitacheck.domain.notification.QNotificationRoutine(forProperty("notificationRoutine"), inits.get("notificationRoutine")) : null;
    }

}

