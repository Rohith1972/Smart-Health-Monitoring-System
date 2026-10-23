package com.shms.alert;

import com.shms.common.ResourceNotFoundException;
import com.shms.patient.Patient;
import com.shms.patient.PatientRepository;
import com.shms.vital.Vital;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final PatientRepository patientRepository;
    private final AlertThresholdConfig thresholds;

    public AlertService(AlertRepository alertRepository,
                        PatientRepository patientRepository,
                        AlertThresholdConfig thresholds) {
        this.alertRepository = alertRepository;
        this.patientRepository = patientRepository;
        this.thresholds = thresholds;
    }

    @Transactional
    public List<Alert> evaluateVitalsAndGenerateAlerts(Vital vital) {
        List<Alert> alerts = new ArrayList<>();
        Patient patient = vital.getPatient();

        // 1. Heart Rate check
        if (vital.getHeartRateBpm() != null) {
            if (vital.getHeartRateBpm() > thresholds.getMaxHeartRate()) {
                String severity = vital.getHeartRateBpm() >= 130 ? "CRITICAL" : "HIGH";
                String msg = String.format("Elevated heart rate detected: %d bpm (Demonstration threshold: max %d bpm)",
                        vital.getHeartRateBpm(), thresholds.getMaxHeartRate());
                alerts.add(new Alert(patient, vital, "HIGH_HEART_RATE", msg, severity));
            } else if (vital.getHeartRateBpm() < thresholds.getMinHeartRate()) {
                String severity = vital.getHeartRateBpm() <= 45 ? "CRITICAL" : "MEDIUM";
                String msg = String.format("Low heart rate detected: %d bpm (Demonstration threshold: min %d bpm)",
                        vital.getHeartRateBpm(), thresholds.getMinHeartRate());
                alerts.add(new Alert(patient, vital, "LOW_HEART_RATE", msg, severity));
            }
        }

        // 2. Oxygen Saturation check
        if (vital.getOxygenSaturation() != null && vital.getOxygenSaturation() < thresholds.getMinOxygenSaturation()) {
            String severity = vital.getOxygenSaturation() < 90.0 ? "CRITICAL" : "HIGH";
            String msg = String.format("Low oxygen saturation: %.1f%% (Demonstration threshold: min %.1f%%)",
                    vital.getOxygenSaturation(), thresholds.getMinOxygenSaturation());
            alerts.add(new Alert(patient, vital, "LOW_OXYGEN_SATURATION", msg, severity));
        }

        // 3. Temperature check
        if (vital.getTemperatureCelsius() != null) {
            if (vital.getTemperatureCelsius() > thresholds.getMaxTemperature()) {
                String severity = vital.getTemperatureCelsius() >= 39.0 ? "HIGH" : "MEDIUM";
                String msg = String.format("High body temperature: %.1f°C (Demonstration threshold: max %.1f°C)",
                        vital.getTemperatureCelsius(), thresholds.getMaxTemperature());
                alerts.add(new Alert(patient, vital, "HIGH_TEMPERATURE", msg, severity));
            } else if (vital.getTemperatureCelsius() < thresholds.getMinTemperature()) {
                String msg = String.format("Low body temperature: %.1f°C (Demonstration threshold: min %.1f°C)",
                        vital.getTemperatureCelsius(), thresholds.getMinTemperature());
                alerts.add(new Alert(patient, vital, "LOW_TEMPERATURE", msg, "MEDIUM"));
            }
        }

        // 4. Blood Pressure check
        if (vital.getSystolicBloodPressure() != null && vital.getDiastolicBloodPressure() != null) {
            if (vital.getSystolicBloodPressure() > thresholds.getMaxSystolicBp()
                    || vital.getDiastolicBloodPressure() > thresholds.getMaxDiastolicBp()) {
                String severity = (vital.getSystolicBloodPressure() >= 140 || vital.getDiastolicBloodPressure() >= 90) ? "HIGH" : "MEDIUM";
                String msg = String.format("Elevated blood pressure: %d/%d mmHg (Demonstration threshold: max %d/%d mmHg)",
                        vital.getSystolicBloodPressure(), vital.getDiastolicBloodPressure(),
                        thresholds.getMaxSystolicBp(), thresholds.getMaxDiastolicBp());
                alerts.add(new Alert(patient, vital, "HIGH_BLOOD_PRESSURE", msg, severity));
            }
        }

        if (!alerts.isEmpty()) {
            return alertRepository.saveAll(alerts);
        }
        return alerts;
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getAlertsByPatientId(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id: " + patientId);
        }

        return alertRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(AlertResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlertResponse getAlertById(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));
        return AlertResponse.fromEntity(alert);
    }

    @Transactional
    public AlertResponse acknowledgeAlert(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));
        alert.acknowledge();
        Alert updated = alertRepository.save(alert);
        return AlertResponse.fromEntity(updated);
    }
}
