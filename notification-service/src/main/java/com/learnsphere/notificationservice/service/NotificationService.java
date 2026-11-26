package com.learnsphere.notificationservice.service;

import com.learnsphere.notificationservice.event.EnrollmentConfirmed;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {

    // Stores IDs of events that have already been processed.
    // Used to prevent duplicate notifications.
    private final Set<String> processedEventIds =
            ConcurrentHashMap.newKeySet();

    // Returns true when a notification was produced, false when the event was ignored as a duplicate.
    public boolean processEnrollmentConfirmed(EnrollmentConfirmed event) {

        try {

            String eventId = event.getEventId();

            if (eventId == null) {
                throw new IllegalArgumentException("Event ID cannot be null");
            }

            // Atomic check-and-record: add() returns false if the eventId is already present.
            // If the same event is received again, skip it.
            if (!processedEventIds.add(eventId)) {

                System.out.println("Duplicate event ignored: " + eventId);

                return false;
            }

            try {
                System.out.println("EnrollmentConfirmed event received");
                System.out.println("Event ID: " + eventId);
                System.out.println("Enrollment ID: " + event.getEnrollmentId());
                System.out.println(
                        "Enrollment confirmed successfully for Learner "
                                + event.getLearnerId()
                                + " for Course "
                                + event.getCourseId()
                                + "."
                );
            } catch (RuntimeException processingFailure) {
                // Processing failed: forget the eventId so a redelivery can be processed.
                processedEventIds.remove(eventId);
                throw processingFailure;
            }

            return true;

        } catch (Exception exception) {

            System.err.println(
                    "Failed to process enrollment notification: "
                            + exception.getMessage()
            );

            throw exception;
        }
    }
}
