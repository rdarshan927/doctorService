package com.example.doctorservice.service;

import com.example.doctorservice.model.DoctorSlot;

import java.util.UUID;

public interface SlotService {

    DoctorSlot reserveSlot(UUID slotId, DoctorSlot slotData);

    DoctorSlot releaseSlot(UUID slotId);

    DoctorSlot getSlotById(UUID slotId);
}
