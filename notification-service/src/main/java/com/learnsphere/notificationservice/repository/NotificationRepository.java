package com.learnsphere.notificationservice.repository;

import com.learnsphere.notificationservice.document.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

// Provides built-in CRUD operations for Notification documents in MongoDB.
public interface NotificationRepository extends MongoRepository<Notification, String> {
}
