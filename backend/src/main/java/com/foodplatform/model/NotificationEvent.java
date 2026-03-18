package com.foodplatform.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {

    public enum Type {
        ORDER_STATUS_CHANGED,
        NEW_ORDER,
        ORDER_ASSIGNED
    }

    private Type type;
    private String orderId;
    private String message;
    private LocalDateTime timestamp;

    public static NotificationEvent orderStatusChanged(String orderId, String message) {
        return new NotificationEvent(Type.ORDER_STATUS_CHANGED, orderId, message, LocalDateTime.now());
    }

    public static NotificationEvent newOrder(String orderId, String message) {
        return new NotificationEvent(Type.NEW_ORDER, orderId, message, LocalDateTime.now());
    }

    public static NotificationEvent orderAssigned(String orderId, String message) {
        return new NotificationEvent(Type.ORDER_ASSIGNED, orderId, message, LocalDateTime.now());
    }
}
