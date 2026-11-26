package com.learnsphere.notificationservice.service;

import com.learnsphere.notificationservice.document.Notification;
import com.learnsphere.notificationservice.event.EnrollmentConfirmed;
import com.learnsphere.notificationservice.exception.NotificationPersistenceException;
import com.learnsphere.notificationservice.repository.NotificationRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public boolean saveNotification(EnrollmentConfirmed event) {

        String eventId = event.getEventId();

        if (eventId == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }

        // Idempotency stored in MongoDB: survives Notification Service restarts.
        // If the same event is received again, skip it.
        if (notificationRepository.findByEventId(eventId).isPresent()) {
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
            notificationRepository.save(notification);

        } catch (DuplicateKeyException duplicate) {
            // Race: another delivery of the same event was saved first.
            // The unique eventId index rejected this copy, so no duplicate document exists.
            System.out.println("Duplicate event ignored: " + eventId);
            return false;

        } catch (Exception ex) {
            // Convert MongoDB persistence failures into an application-specific exception.
            throw new NotificationPersistenceException(
                    "Failed to save notification",
                    ex
            );
        }

        System.out.println(message);
        return true;
    }

    @Override
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}
