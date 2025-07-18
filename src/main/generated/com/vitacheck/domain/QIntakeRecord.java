package com.vitacheck.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QIntakeRecord is a Querydsl query type for IntakeRecord
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QIntakeRecord extends EntityPathBase<IntakeRecord> {

    private static final long serialVersionUID = -189039180L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QIntakeRecord intakeRecord = new QIntakeRecord("intakeRecord");

    public final DatePath<java.time.LocalDate> date = createDate("date", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isTaken = createBoolean("isTaken");

    public final com.vitacheck.domain.notification.QNotificationRoutine notificationRoutine;

    public final com.vitacheck.domain.user.QUser user;

    public QIntakeRecord(String variable) {
        this(IntakeRecord.class, forVariable(variable), INITS);
    }

    public QIntakeRecord(Path<? extends IntakeRecord> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QIntakeRecord(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QIntakeRecord(PathMetadata metadata, PathInits inits) {
        this(IntakeRecord.class, metadata, inits);
    }

    public QIntakeRecord(Class<? extends IntakeRecord> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.notificationRoutine = inits.isInitialized("notificationRoutine") ? new com.vitacheck.domain.notification.QNotificationRoutine(forProperty("notificationRoutine"), inits.get("notificationRoutine")) : null;
        this.user = inits.isInitialized("user") ? new com.vitacheck.domain.user.QUser(forProperty("user")) : null;
    }

}

