package com.example.doctorservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request body for POST /api/slots/{slotId}/reserve
 * <p>
 * Called by the appointment-service as part of the booking saga.
 * <pre>
 * {
 *   "patientId":     "uuid-of-patient",
 *   "appointmentId": "uuid-of-appointment"   // may be null during 2-phase booking
 * }
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReserveSlotRequest {

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    /** May be null if the appointment record is created after slot reservation. */
    private UUID appointmentId;
}
