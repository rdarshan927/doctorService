package com.example.doctorservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Request body for POST /api/doctors/{id}/slots
 * <p>
 * Supports bulk creation — send one or many SlotEntry objects in a single call.
 * <pre>
 * {
 *   "slots": [
 *     { "date": "2026-03-10", "startTime": "09:00", "endTime": "09:30" },
 *     { "date": "2026-03-10", "startTime": "09:30", "endTime": "10:00" }
 *   ]
 * }
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotRequest {

    @NotEmpty(message = "At least one slot entry is required")
    @Valid
    private List<SlotEntry> slots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotEntry {

        @NotNull(message = "Date is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;

        @NotNull(message = "Start time is required")
        @JsonFormat(pattern = "HH:mm")
        private LocalTime startTime;

        @NotNull(message = "End time is required")
        @JsonFormat(pattern = "HH:mm")
        private LocalTime endTime;
    }
}
