package com.vitacheck.domain.notification;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNotificationLog is a Querydsl query type for NotificationLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotificationLog extends EntityPathBase<NotificationLog> {

    private static final long serialVersionUID = 1339182125L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNotificationLog notificationLog = new QNotificationLog("notificationLog");

    public final StringPath body = createString("body");

    public final EnumPath<NotificationChannel> channel = createEnum("channel", NotificationChannel.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> sentAt = createDateTime("sentAt", java.time.LocalDateTime.class);

    public final StringPath targetUrl = createString("targetUrl");

    public final StringPath title = createString("title");

    public final EnumPath<NotificationType> type = createEnum("type", NotificationType.class);

    public final com.vitacheck.user.domain.QUser user;

    public QNotificationLog(String variable) {
        this(NotificationLog.class, forVariable(variable), INITS);
    }

    public QNotificationLog(Path<? extends NotificationLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNotificationLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNotificationLog(PathMetadata metadata, PathInits inits) {
        this(NotificationLog.class, metadata, inits);
    }

    public QNotificationLog(Class<? extends NotificationLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.vitacheck.user.domain.QUser(forProperty("user")) : null;
    }

}

