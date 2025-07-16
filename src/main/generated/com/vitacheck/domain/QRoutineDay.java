package com.vitacheck.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRoutineDay is a Querydsl query type for RoutineDay
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoutineDay extends EntityPathBase<RoutineDay> {

    private static final long serialVersionUID = -1184649969L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRoutineDay routineDay = new QRoutineDay("routineDay");

    public final EnumPath<RoutineDayOfWeek> dayOfWeek = createEnum("dayOfWeek", RoutineDayOfWeek.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QNotificationRoutine notificationRoutine;

    public QRoutineDay(String variable) {
        this(RoutineDay.class, forVariable(variable), INITS);
    }

    public QRoutineDay(Path<? extends RoutineDay> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRoutineDay(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRoutineDay(PathMetadata metadata, PathInits inits) {
        this(RoutineDay.class, metadata, inits);
    }

    public QRoutineDay(Class<? extends RoutineDay> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.notificationRoutine = inits.isInitialized("notificationRoutine") ? new QNotificationRoutine(forProperty("notificationRoutine"), inits.get("notificationRoutine")) : null;
    }

}

