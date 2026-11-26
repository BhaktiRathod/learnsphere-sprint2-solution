package com.learnsphere.notificationservice.service;

import com.learnsphere.notificationservice.document.Notification;
import com.learnsphere.notificationservice.event.EnrollmentConfirmed;
import com.learnsphere.notificationservice.exception.NotificationNotFoundException;
import com.learnsphere.notificationservice.repository.NotificationRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // Processes an EnrollmentConfirmed event and stores the notification in MongoDB.
    // Returns true when a notification was stored, false when the event was ignored as a duplicate.
    public boolean processEnrollmentConfirmed(EnrollmentConfirmed event) {

        String eventId = event.getEventId();

        if (eventId == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }

        // Idempotency check against MongoDB: skip events that were already stored.
        if (notificationRepository.existsByEventId(eventId)) {
            System.out.println("Duplicate event ignored: " + eventId);
            return false;
        }

        String message = "Enrollment confirmed successfully for Learner "
                + event.getLearnerId()
                + " for Course "
                + event.getCourseId()
                + ".";

        Notification notification = new Notification(
                eventId,
                event.getEnrollmentId(),
                event.getLearnerId(),
                event.getCourseId(),
                message
        );

        try {
            // Persist only after the notification has been created successfully.
            notificationRepository.save(notification);
        } catch (DuplicateKeyException duplicate) {
            // The unique index on eventId rejected a concurrent duplicate.
            System.out.println("Duplicate event ignored: " + eventId);
            return false;
        }

        System.out.println(message);
        System.out.println("Notification saved to MongoDB for event: " + eventId);

        return true;
    }

    // Fetches all notification documents stored in MongoDB.
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    // Fetches one notification document by its MongoDB id.
    public Notification getNotificationById(String id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found with id: " + id));
    }
}
