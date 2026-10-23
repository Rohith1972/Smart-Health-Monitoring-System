package com.shms.doctor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DoctorRequest(
        @NotBlank(message = "Doctor full name is required")
        String fullName,

        @NotBlank(message = "Doctor email is required")
        @Email(message = "Please provide a valid email address")
        String email,

        String specialization,

        String phone
) {}
