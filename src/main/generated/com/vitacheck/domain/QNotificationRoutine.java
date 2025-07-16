package com.vitacheck.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNotificationRoutine is a Querydsl query type for NotificationRoutine
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotificationRoutine extends EntityPathBase<NotificationRoutine> {

    private static final long serialVersionUID = -722027934L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNotificationRoutine notificationRoutine = new QNotificationRoutine("notificationRoutine");

    public final QBaseTimeEntity _super = new QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isEnabled = createBoolean("isEnabled");

    public final ListPath<RoutineDay, QRoutineDay> routineDays = this.<RoutineDay, QRoutineDay>createList("routineDays", RoutineDay.class, QRoutineDay.class, PathInits.DIRECT2);

    public final ListPath<RoutineTime, QRoutineTime> routineTimes = this.<RoutineTime, QRoutineTime>createList("routineTimes", RoutineTime.class, QRoutineTime.class, PathInits.DIRECT2);

    public final QSupplement supplement;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.vitacheck.domain.user.QUser user;

    public QNotificationRoutine(String variable) {
        this(NotificationRoutine.class, forVariable(variable), INITS);
    }

    public QNotificationRoutine(Path<? extends NotificationRoutine> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNotificationRoutine(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNotificationRoutine(PathMetadata metadata, PathInits inits) {
        this(NotificationRoutine.class, metadata, inits);
    }

    public QNotificationRoutine(Class<? extends NotificationRoutine> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.supplement = inits.isInitialized("supplement") ? new QSupplement(forProperty("supplement"), inits.get("supplement")) : null;
        this.user = inits.isInitialized("user") ? new com.vitacheck.domain.user.QUser(forProperty("user")) : null;
    }

}

