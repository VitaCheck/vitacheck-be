package com.vitacheck.domain.notification;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNotificationSettings is a Querydsl query type for NotificationSettings
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotificationSettings extends EntityPathBase<NotificationSettings> {

    private static final long serialVersionUID = -1552663334L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNotificationSettings notificationSettings = new QNotificationSettings("notificationSettings");

    public final EnumPath<NotificationChannel> channel = createEnum("channel", NotificationChannel.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isEnabled = createBoolean("isEnabled");

    public final EnumPath<NotificationType> type = createEnum("type", NotificationType.class);

    public final com.vitacheck.domain.user.QUser user;

    public QNotificationSettings(String variable) {
        this(NotificationSettings.class, forVariable(variable), INITS);
    }

    public QNotificationSettings(Path<? extends NotificationSettings> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNotificationSettings(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNotificationSettings(PathMetadata metadata, PathInits inits) {
        this(NotificationSettings.class, metadata, inits);
    }

    public QNotificationSettings(Class<? extends NotificationSettings> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.vitacheck.domain.user.QUser(forProperty("user")) : null;
    }

}

