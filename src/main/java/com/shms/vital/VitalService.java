package com.shms.vital;

import com.shms.common.ResourceNotFoundException;
import com.shms.patient.Patient;
import com.shms.patient.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VitalService {

    private final VitalRepository vitalRepository;
    private final PatientRepository patientRepository;
    private final com.shms.alert.AlertService alertService;

    public VitalService(VitalRepository vitalRepository,
                        PatientRepository patientRepository,
                        com.shms.alert.AlertService alertService) {
        this.vitalRepository = vitalRepository;
        this.patientRepository = patientRepository;
        this.alertService = alertService;
    }

    @Transactional
    public VitalResponse recordVital(Long patientId, VitalRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        Vital vital = new Vital(
                patient,
                request.heartRateBpm(),
                request.systolicBloodPressure(),
                request.diastolicBloodPressure(),
                request.oxygenSaturation(),
                request.temperatureCelsius(),
                request.recordedAt()
        );

        Vital saved = vitalRepository.save(vital);
        alertService.evaluateVitalsAndGenerateAlerts(saved);
        return VitalResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<VitalResponse> getVitalsByPatientId(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id: " + patientId);
        }

        return vitalRepository.findByPatientIdOrderByRecordedAtDesc(patientId)
                .stream()
                .map(VitalResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public VitalResponse getLatestVitalByPatientId(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id: " + patientId);
        }

        Vital latest = vitalRepository.findFirstByPatientIdOrderByRecordedAtDesc(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("No vitals recorded yet for patient id: " + patientId));
        return VitalResponse.fromEntity(latest);
    }

    @Transactional(readOnly = true)
    public VitalResponse getVitalById(Long id) {
        Vital vital = vitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vital record not found with id: " + id));
        return VitalResponse.fromEntity(vital);
    }
}
