package com.example.doctorservice.dto;

import com.example.doctorservice.model.SlotStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotResponse {

    private UUID id;
    private UUID doctorId;
    private String doctorName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private SlotStatus status;

    /** Patient UUID that reserved this slot (null when AVAILABLE). */
    private UUID reservedBy;

    /** Appointment UUID linked to this slot (null when AVAILABLE). */
    private UUID appointmentId;

    private LocalDateTime createdAt;
}
