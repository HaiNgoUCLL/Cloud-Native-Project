package com.foodplatform.service;

import com.foodplatform.model.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.*;

class NotificationServiceTest {

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
    }

    @Test
    void subscribe_createsEmitter() {
        SseEmitter emitter = notificationService.subscribe("user1");
        assertThat(emitter).isNotNull();
    }

    @Test
    void sendNotification_toConnectedUser_delivers() {
        SseEmitter emitter = notificationService.subscribe("user1");
        NotificationEvent event = NotificationEvent.newOrder("order1", "New order received");

        assertThatCode(() -> notificationService.sendNotification("user1", event))
                .doesNotThrowAnyException();
    }

    @Test
    void sendNotification_noConnection_noop() {
        NotificationEvent event = NotificationEvent.orderStatusChanged("order1", "Order confirmed");

        assertThatCode(() -> notificationService.sendNotification("nonexistentUser", event))
                .doesNotThrowAnyException();
    }

    @Test
    void subscribe_multipleUsers_independent() {
        SseEmitter emitter1 = notificationService.subscribe("user1");
        SseEmitter emitter2 = notificationService.subscribe("user2");

        assertThat(emitter1).isNotSameAs(emitter2);
    }

    @Test
    void notificationEvent_factoryMethods_createCorrectTypes() {
        NotificationEvent newOrder = NotificationEvent.newOrder("o1", "New order");
        NotificationEvent statusChanged = NotificationEvent.orderStatusChanged("o2", "Status changed");
        NotificationEvent assigned = NotificationEvent.orderAssigned("o3", "Assigned");

        assertThat(newOrder.getType()).isEqualTo(NotificationEvent.Type.NEW_ORDER);
        assertThat(newOrder.getOrderId()).isEqualTo("o1");
        assertThat(newOrder.getMessage()).isEqualTo("New order");
        assertThat(newOrder.getTimestamp()).isNotNull();

        assertThat(statusChanged.getType()).isEqualTo(NotificationEvent.Type.ORDER_STATUS_CHANGED);
        assertThat(assigned.getType()).isEqualTo(NotificationEvent.Type.ORDER_ASSIGNED);
    }

    @Test
    void notificationEvent_orderReady_createsCorrectType() {
        NotificationEvent ready = NotificationEvent.orderReady("o4", "Order ready");

        assertThat(ready.getType()).isEqualTo(NotificationEvent.Type.ORDER_READY);
        assertThat(ready.getOrderId()).isEqualTo("o4");
        assertThat(ready.getMessage()).isEqualTo("Order ready");
    }
}
