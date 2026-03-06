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
}
