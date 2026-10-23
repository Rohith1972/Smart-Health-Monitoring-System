package com.shms.security;

public record AuthResponse(
        String token,
        String tokenType,
        String username,
        Role role,
        Long patientId,
        Long doctorId,
        long expiresIn
) {
    public AuthResponse(String token, String username, Role role, Long patientId, Long doctorId, long expiresIn) {
        this(token, "Bearer", username, role, patientId, doctorId, expiresIn);
    }
}
