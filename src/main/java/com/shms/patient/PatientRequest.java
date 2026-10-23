package com.shms.patient;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record PatientRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        String email,

        String phone,

        LocalDate dateOfBirth,

        String gender,

        String bloodGroup,

        String address,

        String medicalNotes
) {}
