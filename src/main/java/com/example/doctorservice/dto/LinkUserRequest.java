package com.example.doctorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request body for PATCH /api/doctors/{id}/link-user.
 * Links a user-service account UUID to an existing doctor profile.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkUserRequest {

    /** The user-service UUID of the DOCTOR-role account to link. */
    private UUID userId;
}
