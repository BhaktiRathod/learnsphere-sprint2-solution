package com.learnsphere.notificationservice.service;

import com.learnsphere.notificationservice.document.Notification;
import com.learnsphere.notificationservice.event.EnrollmentConfirmed;

import java.util.List;

public interface NotificationService {

    // Saves a notification received from the EnrollmentConfirmed event.
    // Returns true when the notification was saved, false when the event was ignored as a duplicate.
    boolean saveNotification(EnrollmentConfirmed event);

    List<Notification> getAllNotifications();
}
