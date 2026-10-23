package com.shms.doctor;

import com.shms.alert.Alert;
import com.shms.alert.AlertRepository;
import com.shms.alert.AlertResponse;
import com.shms.common.DuplicateResourceException;
import com.shms.common.ResourceNotFoundException;
import com.shms.patient.Patient;
import com.shms.patient.PatientRepository;
import com.shms.patient.PatientResponse;
import com.shms.vital.Vital;
import com.shms.vital.VitalRepository;
import com.shms.vital.VitalResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final VitalRepository vitalRepository;
    private final AlertRepository alertRepository;

    public DoctorService(DoctorRepository doctorRepository,
                         PatientRepository patientRepository,
                         VitalRepository vitalRepository,
                         AlertRepository alertRepository) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.vitalRepository = vitalRepository;
        this.alertRepository = alertRepository;
    }

    @Transactional
    public DoctorResponse createDoctor(DoctorRequest request) {
        if (doctorRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("A doctor with email '" + request.email() + "' already exists.");
        }

        Doctor doctor = new Doctor(
                request.fullName(),
                request.email(),
                request.specialization(),
                request.phone()
        );

        Doctor saved = doctorRepository.save(doctor);
        return DoctorResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(DoctorResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        return DoctorResponse.fromEntity(doctor);
    }

    @Transactional
    public PatientResponse assignPatientToDoctor(Long doctorId, Long patientId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        patient.setAssignedDoctor(doctor);
        Patient updated = patientRepository.save(patient);
        return PatientResponse.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> getAssignedPatients(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id: " + doctorId);
        }

        return patientRepository.findByAssignedDoctorId(doctorId)
                .stream()
                .map(PatientResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DoctorDashboardResponse getDoctorDashboard(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        List<Patient> assignedPatients = patientRepository.findByAssignedDoctorId(doctorId);
        List<DoctorDashboardResponse.PatientSummary> patientSummaries = new ArrayList<>();
        long totalActiveAlerts = 0;

        for (Patient patient : assignedPatients) {
            VitalResponse latestVital = vitalRepository
                    .findFirstByPatientIdOrderByRecordedAtDesc(patient.getId())
                    .map(VitalResponse::fromEntity)
                    .orElse(null);

            List<Alert> activeAlertEntities = alertRepository
                    .findByPatientIdAndStatusOrderByCreatedAtDesc(patient.getId(), "ACTIVE");

            totalActiveAlerts += activeAlertEntities.size();

            List<AlertResponse> activeAlerts = activeAlertEntities.stream()
                    .map(AlertResponse::fromEntity)
                    .toList();

            patientSummaries.add(new DoctorDashboardResponse.PatientSummary(
                    patient.getId(),
                    patient.getFullName(),
                    patient.getEmail(),
                    patient.getPhone(),
                    patient.getBloodGroup(),
                    latestVital,
                    activeAlerts
            ));
        }

        return new DoctorDashboardResponse(
                doctor.getId(),
                doctor.getFullName(),
                doctor.getSpecialization(),
                assignedPatients.size(),
                totalActiveAlerts,
                patientSummaries
        );
    }
}
