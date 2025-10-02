package com.vitacheck.notification.domain;

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

    private static final long serialVersionUID = -756288129L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNotificationRoutine notificationRoutine = new QNotificationRoutine("notificationRoutine");

    public final com.vitacheck.common.entity.QBaseTimeEntity _super = new com.vitacheck.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QCustomSupplement customSupplement;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<IntakeRecord, QIntakeRecord> intakeRecords = this.<IntakeRecord, QIntakeRecord>createList("intakeRecords", IntakeRecord.class, QIntakeRecord.class, PathInits.DIRECT2);

    public final BooleanPath isEnabled = createBoolean("isEnabled");

    public final ListPath<RoutineDetail, QRoutineDetail> routineDetails = this.<RoutineDetail, QRoutineDetail>createList("routineDetails", RoutineDetail.class, QRoutineDetail.class, PathInits.DIRECT2);

    public final com.vitacheck.product.domain.Supplement.QSupplement supplement;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.vitacheck.user.domain.QUser user;

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
        this.customSupplement = inits.isInitialized("customSupplement") ? new QCustomSupplement(forProperty("customSupplement"), inits.get("customSupplement")) : null;
        this.supplement = inits.isInitialized("supplement") ? new com.vitacheck.product.domain.Supplement.QSupplement(forProperty("supplement"), inits.get("supplement")) : null;
        this.user = inits.isInitialized("user") ? new com.vitacheck.user.domain.QUser(forProperty("user")) : null;
    }

}

