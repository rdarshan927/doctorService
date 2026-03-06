package com.example.doctorservice.service;

import com.example.doctorservice.dto.DoctorRequest;
import com.example.doctorservice.dto.DoctorResponse;
import com.example.doctorservice.dto.SlotRequest;
import com.example.doctorservice.dto.SlotResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DoctorService {

    /** Create a new doctor profile. */
    DoctorResponse createDoctor(DoctorRequest request);

    /** Retrieve a single doctor by their UUID. */
    DoctorResponse getDoctorById(UUID id);

    /**
     * List all doctors.
     * Optionally filter by specialization or department (pass null to skip filter).
     */
    List<DoctorResponse> getAllDoctors(String specialization, String department);

    /**
     * Pre-create one or many availability slots for a doctor.
     * Used by POST /api/doctors/{id}/slots.
     */
    List<SlotResponse> createSlots(UUID doctorId, SlotRequest request);

    /**
     * Return all slots (AVAILABLE + RESERVED) for a doctor on the given date,
     * ordered by start time.
     * Used by GET /api/doctors/{id}/slots?date=YYYY-MM-DD.
     */
    List<SlotResponse> getSlotsByDate(UUID doctorId, LocalDate date);

    /**
     * Link (or update) the user-service account UUID on an existing doctor profile.
     * Called by PATCH /api/doctors/{id}/link-user.
     */
    DoctorResponse linkUser(UUID doctorId, UUID userId);

    /**
     * Full update of a doctor profile (used by receptionist during verification).
     * When request.isVerified() == true the service enforces that specialization
     * and licenseNumber are non-blank.
     */
    DoctorResponse updateDoctor(UUID id, DoctorRequest request);

    /**
     * Toggle the verified flag only (quick approve / revoke by admin or receptionist).
     * Called by PATCH /api/doctors/{id}/verify.
     */
    DoctorResponse verifyDoctor(UUID id, boolean verified);
}
