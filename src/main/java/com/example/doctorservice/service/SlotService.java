package com.example.doctorservice.service;

import com.example.doctorservice.dto.ReserveSlotRequest;
import com.example.doctorservice.dto.SlotResponse;
import com.example.doctorservice.model.DoctorSlot;

import java.util.UUID;

public interface SlotService {

    SlotResponse reserveSlot(UUID slotId, ReserveSlotRequest request);

    DoctorSlot releaseSlot(UUID slotId);

    SlotResponse getSlotById(UUID slotId);
}
