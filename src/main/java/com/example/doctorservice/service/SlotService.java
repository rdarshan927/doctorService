package com.example.doctorservice.service;

import com.example.doctorservice.dto.ReserveSlotRequest;
import com.example.doctorservice.dto.SlotResponse;

import java.util.UUID;

public interface SlotService {

    /**
     * Reserve a slot for a patient.
     * Changes status AVAILABLE → RESERVED.
     * Called by the appointment-service as part of the booking saga.
     */
    SlotResponse reserveSlot(UUID slotId, ReserveSlotRequest request);

    /**
     * Release a reserved slot back to AVAILABLE.
     * Saga compensation endpoint — called when a downstream step fails
     * (e.g. payment declined) and the booking must be rolled back.
     */
    SlotResponse releaseSlot(UUID slotId);

    /** Retrieve a single slot by ID. */
    SlotResponse getSlotById(UUID slotId);
}
