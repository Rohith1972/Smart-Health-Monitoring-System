package com.shms.vital;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record VitalRequest(
        @NotNull(message = "Heart rate is required")
        @Min(value = 20, message = "Heart rate must be at least 20 bpm")
        @Max(value = 300, message = "Heart rate cannot exceed 300 bpm")
        Integer heartRateBpm,

        @NotNull(message = "Systolic blood pressure is required")
        @Min(value = 40, message = "Systolic blood pressure must be at least 40 mmHg")
        @Max(value = 300, message = "Systolic blood pressure cannot exceed 300 mmHg")
        Integer systolicBloodPressure,

        @NotNull(message = "Diastolic blood pressure is required")
        @Min(value = 30, message = "Diastolic blood pressure must be at least 30 mmHg")
        @Max(value = 200, message = "Diastolic blood pressure cannot exceed 200 mmHg")
        Integer diastolicBloodPressure,

        @NotNull(message = "Oxygen saturation is required")
        @Min(value = 50, message = "Oxygen saturation must be at least 50%")
        @Max(value = 100, message = "Oxygen saturation cannot exceed 100%")
        Double oxygenSaturation,

        @NotNull(message = "Temperature is required")
        @Min(value = 25, message = "Temperature must be at least 25°C")
        @Max(value = 45, message = "Temperature cannot exceed 45°C")
        Double temperatureCelsius,

        LocalDateTime recordedAt
) {}
