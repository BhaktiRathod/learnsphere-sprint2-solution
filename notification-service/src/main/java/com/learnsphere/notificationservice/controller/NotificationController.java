package com.learnsphere.notificationservice.controller;

import com.learnsphere.notificationservice.document.Notification;
import com.learnsphere.notificationservice.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Fetches all notification documents stored in MongoDB.
    @GetMapping
    public List<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }
}
