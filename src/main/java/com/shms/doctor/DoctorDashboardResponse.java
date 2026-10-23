package com.shms.doctor;

import com.shms.alert.AlertResponse;
import com.shms.vital.VitalResponse;

import java.util.List;

public record DoctorDashboardResponse(
        Long doctorId,
        String doctorName,
        String specialization,
        int totalPatients,
        long totalActiveAlerts,
        List<PatientSummary> patients
) {
    public record PatientSummary(
            Long patientId,
            String patientName,
            String email,
            String phone,
            String bloodGroup,
            VitalResponse latestVital,
            List<AlertResponse> activeAlerts
    ) {}
}
