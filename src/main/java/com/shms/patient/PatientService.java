package com.shms.patient;

import com.shms.common.DuplicateResourceException;
import com.shms.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public PatientResponse registerPatient(PatientRequest request) {
        if (patientRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("A patient with email '" + request.email() + "' already exists.");
        }

        Patient patient = new Patient(
                request.fullName(),
                request.email(),
                request.phone(),
                request.dateOfBirth(),
                request.gender(),
                request.bloodGroup(),
                request.address(),
                request.medicalNotes()
        );

        Patient saved = patientRepository.save(patient);
        return PatientResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(PatientResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return PatientResponse.fromEntity(patient);
    }

    @Transactional
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        // Check if email is being changed to an email that is already registered by another patient
        if (!patient.getEmail().equalsIgnoreCase(request.email())
                && patientRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("A patient with email '" + request.email() + "' already exists.");
        }

        patient.setFullName(request.fullName());
        patient.setEmail(request.email());
        patient.setPhone(request.phone());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setBloodGroup(request.bloodGroup());
        patient.setAddress(request.address());
        patient.setMedicalNotes(request.medicalNotes());

        Patient updated = patientRepository.save(patient);
        return PatientResponse.fromEntity(updated);
    }

    @Transactional
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        patientRepository.delete(patient);
    }
}
