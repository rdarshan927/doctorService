package com.example.doctorservice.service;

import com.example.doctorservice.dto.SlotResponse;
import com.example.doctorservice.model.DoctorSlot;

import java.util.UUID;

public interface SlotService {

    DoctorSlot reserveSlot(UUID slotId, DoctorSlot slotData);

    DoctorSlot releaseSlot(UUID slotId);

    SlotResponse getSlotById(UUID slotId);
}
