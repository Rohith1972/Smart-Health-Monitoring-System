package com.shms.alert;

import java.time.LocalDateTime;

public record AlertResponse(
        Long id,
        Long patientId,
        String patientName,
        Long vitalId,
        String alertType,
        String message,
        String severity,
        String status,
        LocalDateTime createdAt,
        LocalDateTime acknowledgedAt
) {
    public static AlertResponse fromEntity(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getPatient().getId(),
                alert.getPatient().getFullName(),
                alert.getVital().getId(),
                alert.getAlertType(),
                alert.getMessage(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getCreatedAt(),
                alert.getAcknowledgedAt()
        );
    }
}
