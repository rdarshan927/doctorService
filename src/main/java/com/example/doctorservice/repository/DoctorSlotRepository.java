package com.example.doctorservice.repository;

import com.example.doctorservice.model.DoctorSlot;
import com.example.doctorservice.model.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorSlotRepository extends JpaRepository<DoctorSlot, UUID> {

    /**
     * All slots for a doctor on a specific date, ordered by start time.
     * Used by GET /api/doctors/{id}/slots?date=YYYY-MM-DD
     */
    List<DoctorSlot> findByDoctorIdAndDateOrderByStartTimeAsc(UUID doctorId, LocalDate date);

    /**
     * Only AVAILABLE slots for a doctor on a specific date.
     * Useful for booking UIs that hide already-reserved slots.
     */
    @Query("""
            SELECT s FROM DoctorSlot s
            WHERE s.doctor.id = :doctorId
              AND s.date       = :date
              AND s.status     = 'AVAILABLE'
            ORDER BY s.startTime ASC
            """)
    List<DoctorSlot> findAvailableByDoctorAndDate(
            @Param("doctorId") UUID doctorId,
            @Param("date") LocalDate date);

    /**
     * Slots for a doctor across a date range (e.g., weekly view).
     */
    List<DoctorSlot> findByDoctorIdAndDateBetweenOrderByDateAscStartTimeAsc(
            UUID doctorId, LocalDate from, LocalDate to);

    /**
     * Pessimistic write lock — used in reserve() to prevent double-booking.
     * SELECT ... FOR UPDATE is issued; other transactions block until commit.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DoctorSlot s WHERE s.id = :id")
    Optional<DoctorSlot> findByIdForUpdate(@Param("id") UUID id);

    List<DoctorSlot> findByStatus(SlotStatus status);

    /** Slots held by a specific appointment (for saga compensation lookups). */
    List<DoctorSlot> findByAppointmentId(UUID appointmentId);
}
