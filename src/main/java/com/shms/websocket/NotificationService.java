package com.shms.websocket;

import com.shms.alert.AlertResponse;
import com.shms.vital.VitalResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastVitalReading(VitalResponse vital, Long assignedDoctorId) {
        // Broadcast to general vitals topic
        messagingTemplate.convertAndSend("/topic/vitals", vital);

        // If patient is assigned to a doctor, also send to that doctor's private channel
        if (assignedDoctorId != null) {
            messagingTemplate.convertAndSend("/topic/doctor/" + assignedDoctorId + "/vitals", vital);
        }
    }

    public void broadcastAlert(AlertResponse alert, Long assignedDoctorId) {
        // Broadcast to general alerts topic
        messagingTemplate.convertAndSend("/topic/alerts", alert);

        // If patient is assigned to a doctor, notify doctor's alert channel
        if (assignedDoctorId != null) {
            messagingTemplate.convertAndSend("/topic/doctor/" + assignedDoctorId + "/alerts", alert);
        }
    }
}
