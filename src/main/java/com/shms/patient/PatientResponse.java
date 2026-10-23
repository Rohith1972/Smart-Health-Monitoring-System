package com.shms.patient;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        String gender,
        String bloodGroup,
        String address,
        String medicalNotes,
        Long assignedDoctorId,
        String assignedDoctorName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PatientResponse fromEntity(Patient patient) {
        Long docId = patient.getAssignedDoctor() != null ? patient.getAssignedDoctor().getId() : null;
        String docName = patient.getAssignedDoctor() != null ? patient.getAssignedDoctor().getFullName() : null;

        return new PatientResponse(
                patient.getId(),
                patient.getFullName(),
                patient.getEmail(),
                patient.getPhone(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getBloodGroup(),
                patient.getAddress(),
                patient.getMedicalNotes(),
                docId,
                docName,
                patient.getCreatedAt(),
                patient.getUpdatedAt()
        );
    }
}
