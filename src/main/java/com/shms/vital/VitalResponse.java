package com.shms.vital;

import java.time.LocalDateTime;

public record VitalResponse(
        Long id,
        Long patientId,
        String patientName,
        Integer heartRateBpm,
        Integer systolicBloodPressure,
        Integer diastolicBloodPressure,
        Double oxygenSaturation,
        Double temperatureCelsius,
        LocalDateTime recordedAt
) {
    public static VitalResponse fromEntity(Vital vital) {
        return new VitalResponse(
                vital.getId(),
                vital.getPatient().getId(),
                vital.getPatient().getFullName(),
                vital.getHeartRateBpm(),
                vital.getSystolicBloodPressure(),
                vital.getDiastolicBloodPressure(),
                vital.getOxygenSaturation(),
                vital.getTemperatureCelsius(),
                vital.getRecordedAt()
        );
    }
}
