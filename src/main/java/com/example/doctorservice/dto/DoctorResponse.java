package com.example.doctorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {

    private UUID id;

    /** Linked user-service account UUID (may be null if not yet linked). */
    private UUID userId;

    private String name;
    private String specialization;
    private String email;
    private String phone;
    private String licenseNumber;
    private String department;
    private int yearsOfExperience;
    private boolean isActive;
    private boolean verified;
    private LocalDateTime createdAt;
}
