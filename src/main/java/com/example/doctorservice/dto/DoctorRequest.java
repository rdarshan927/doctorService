package com.example.doctorservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRequest {

    /**
     * Optional: UUID of the matching user-service account (role = DOCTOR).
     * Link this after the user account is created via user-service registration.
     */
    private UUID userId;

    @NotBlank(message = "Name is required")
    private String name;

    /** Nullable until receptionist verifies — allows creating an unverified stub. */
    private String specialization;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;

    private String phone;

    /** Nullable until receptionist verifies — must be unique once set. */
    private String licenseNumber;

    private String department;

    @Min(value = 0, message = "Years of experience cannot be negative")
    private int yearsOfExperience;

    /**
     * Set to true only by ADMIN or RECEPTIONIST after reviewing the application.
     * Defaults to false on creation.
     */
    private boolean verified;
}
