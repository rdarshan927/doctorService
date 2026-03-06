package com.example.doctorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response from user-service GET /api/auth/validate.
 * <p>
 * The doctor-service calls this endpoint to verify a JWT and obtain
 * the caller's userId and role without direct Supabase access.
 * Role is kept as a String to avoid tight coupling with user-service's
 * UserRole enum (matches the JSON value: "ADMIN", "DOCTOR", "PATIENT", etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {

    private boolean valid;
    private UUID userId;
    private String email;

    /** One of: ADMIN, DOCTOR, PATIENT, RECEPTIONIST — as defined in user-service. */
    private String role;

    private String message;
}
