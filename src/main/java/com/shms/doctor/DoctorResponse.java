package com.shms.doctor;

public record DoctorResponse(
        Long id,
        String fullName,
        String email,
        String specialization,
        String phone,
        int assignedPatientsCount
) {
    public static DoctorResponse fromEntity(Doctor doctor) {
        int count = doctor.getAssignedPatients() != null ? doctor.getAssignedPatients().size() : 0;
        return new DoctorResponse(
                doctor.getId(),
                doctor.getFullName(),
                doctor.getEmail(),
                doctor.getSpecialization(),
                doctor.getPhone(),
                count
        );
    }
}
