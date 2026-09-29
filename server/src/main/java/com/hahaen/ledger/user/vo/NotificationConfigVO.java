package com.hahaen.ledger.user.vo;

public record NotificationConfigVO(String notificationType, boolean configured, String notificationKey) {
    public NotificationConfigVO(String notificationType, boolean configured) {
        this(notificationType, configured, null);
    }
}
