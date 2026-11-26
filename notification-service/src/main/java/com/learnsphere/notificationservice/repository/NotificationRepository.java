package com.learnsphere.notificationservice.repository;

import com.learnsphere.notificationservice.document.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

// Provides built-in CRUD operations for Notification documents in MongoDB.
public interface NotificationRepository extends MongoRepository<Notification, String> {

    // Finds the notification already stored for an event (used for restart-safe idempotency).
    Optional<Notification> findByEventId(String eventId);
}
