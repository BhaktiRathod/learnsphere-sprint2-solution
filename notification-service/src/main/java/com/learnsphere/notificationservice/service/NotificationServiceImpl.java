package com.learnsphere.notificationservice.service;

import com.learnsphere.notificationservice.document.Notification;
import com.learnsphere.notificationservice.event.EnrollmentConfirmed;
import com.learnsphere.notificationservice.exception.NotificationPersistenceException;
import com.learnsphere.notificationservice.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    // Idempotency from Sprint 4: IDs of events that have already been processed.
    // Only event IDs are kept here; notification records are stored in MongoDB.
    private final Set<String> processedEventIds =
            ConcurrentHashMap.newKeySet();

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public boolean saveNotification(EnrollmentConfirmed event) {

        String eventId = event.getEventId();

        if (eventId == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }

        // If the same event is received again, skip it.
        // add() returns false when the eventId was already processed.
        if (!processedEventIds.add(eventId)) {
            System.out.println("Duplicate event ignored: " + eventId);
            return false;
        }

        try {
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

            notificationRepository.save(notification);

            System.out.println(message);
            return true;

        } catch (Exception ex) {
            // Saving failed: forget the eventId so a redelivered event can be saved again.
            processedEventIds.remove(eventId);

            // Convert MongoDB persistence failures into an application-specific exception.
            throw new NotificationPersistenceException(
                    "Failed to save notification",
                    ex
            );
        }
    }

    @Override
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}
