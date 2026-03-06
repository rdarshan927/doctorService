package com.example.doctorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request body for PATCH /api/doctors/{id}/verify */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyRequest {
    private boolean verified;
}
