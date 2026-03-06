package com.example.doctorservice;

import com.example.doctorservice.dto.*;
import com.example.doctorservice.exception.DoctorNotFoundException;
import com.example.doctorservice.exception.SlotNotAvailableException;
import com.example.doctorservice.exception.SlotNotFoundException;
import com.example.doctorservice.model.Doctor;
import com.example.doctorservice.model.DoctorSlot;
import com.example.doctorservice.model.SlotStatus;
import com.example.doctorservice.repository.DoctorRepository;
import com.example.doctorservice.repository.DoctorSlotRepository;
import com.example.doctorservice.service.DoctorServiceImpl;
import com.example.doctorservice.service.SlotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    // ── Shared test fixtures ──────────────────────────────────────────────

    @Mock  private DoctorRepository doctorRepository;
    @Mock  private DoctorSlotRepository slotRepository;

    @InjectMocks private DoctorServiceImpl doctorService;
    @InjectMocks private SlotServiceImpl   slotService;

    private UUID      doctorId;
    private UUID      slotId;
    private Doctor    mockDoctor;
    private DoctorSlot availableSlot;
    private DoctorSlot reservedSlot;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        slotId   = UUID.randomUUID();

        mockDoctor = Doctor.builder()
                .id(doctorId)
                .name("Dr. Alice")
                .specialization("Cardiology")
                .email("alice@clinic.com")
                .licenseNumber("LIC-001")
                .isActive(true)
                .build();

        availableSlot = DoctorSlot.builder()
                .id(slotId)
                .doctor(mockDoctor)
                .date(LocalDate.of(2026, 3, 10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .status(SlotStatus.AVAILABLE)
                .build();

        reservedSlot = DoctorSlot.builder()
                .id(slotId)
                .doctor(mockDoctor)
                .date(LocalDate.of(2026, 3, 10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .status(SlotStatus.RESERVED)
                .reservedBy(UUID.randomUUID())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DoctorService tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createDoctor_shouldReturnDoctorResponse() {
        when(doctorRepository.existsByEmail("alice@clinic.com")).thenReturn(false);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(mockDoctor);

        DoctorRequest req = DoctorRequest.builder()
                .name("Dr. Alice")
                .specialization("Cardiology")
                .email("alice@clinic.com")
                .licenseNumber("LIC-001")
                .build();

        DoctorResponse result = doctorService.createDoctor(req);

        assertThat(result.getEmail()).isEqualTo("alice@clinic.com");
        assertThat(result.getName()).isEqualTo("Dr. Alice");
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    void getDoctorById_shouldReturnDoctor_whenExists() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(mockDoctor));

        DoctorResponse result = doctorService.getDoctorById(doctorId);

        assertThat(result.getId()).isEqualTo(doctorId);
        assertThat(result.getSpecialization()).isEqualTo("Cardiology");
    }

    @Test
    void getDoctorById_shouldThrow_whenNotFound() {
        UUID unknown = UUID.randomUUID();
        when(doctorRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.getDoctorById(unknown))
                .isInstanceOf(DoctorNotFoundException.class)
                .hasMessageContaining(unknown.toString());
    }

    @Test
    void createSlots_shouldCreateAndReturnSlots() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(mockDoctor));
        when(slotRepository.saveAll(any())).thenReturn(List.of(availableSlot));

        SlotRequest req = new SlotRequest(List.of(
                new SlotRequest.SlotEntry(
                        LocalDate.of(2026, 3, 10),
                        LocalTime.of(9, 0),
                        LocalTime.of(9, 30)
                )
        ));

        List<SlotResponse> result = doctorService.createSlots(doctorId, req);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(SlotStatus.AVAILABLE);
    }

    @Test
    void getSlotsByDate_shouldThrow_whenDoctorNotFound() {
        UUID unknown = UUID.randomUUID();
        when(doctorRepository.existsById(unknown)).thenReturn(false);

        assertThatThrownBy(() -> doctorService.getSlotsByDate(unknown, LocalDate.now()))
                .isInstanceOf(DoctorNotFoundException.class);
    }

    @Test
    void getAllDoctors_shouldFilterBySpecialization() {
        when(doctorRepository.findBySpecializationIgnoreCase("Cardiology"))
                .thenReturn(List.of(mockDoctor));

        List<DoctorResponse> result = doctorService.getAllDoctors("Cardiology", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSpecialization()).isEqualTo("Cardiology");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SlotService tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void reserveSlot_shouldChangeStatusToReserved() {
        UUID patientId = UUID.randomUUID();
        when(slotRepository.findByIdForUpdate(slotId)).thenReturn(Optional.of(availableSlot));
        when(slotRepository.save(any(DoctorSlot.class))).thenReturn(availableSlot);

        ReserveSlotRequest req = new ReserveSlotRequest(patientId, null);
        SlotResponse result = slotService.reserveSlot(slotId, req);

        assertThat(result.getStatus()).isEqualTo(SlotStatus.RESERVED);
        verify(slotRepository).save(any(DoctorSlot.class));
    }

    @Test
    void reserveSlot_shouldThrow_whenAlreadyReserved() {
        when(slotRepository.findByIdForUpdate(slotId)).thenReturn(Optional.of(reservedSlot));

        assertThatThrownBy(() -> slotService.reserveSlot(slotId, new ReserveSlotRequest(UUID.randomUUID(), null)))
                .isInstanceOf(SlotNotAvailableException.class);
    }

    @Test
    void reserveSlot_shouldThrow_whenSlotNotFound() {
        UUID unknown = UUID.randomUUID();
        when(slotRepository.findByIdForUpdate(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotService.reserveSlot(unknown, new ReserveSlotRequest(UUID.randomUUID(), null)))
                .isInstanceOf(SlotNotFoundException.class);
    }

    @Test
    void releaseSlot_shouldResetToAvailable() {
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(reservedSlot));
        when(slotRepository.save(any(DoctorSlot.class))).thenReturn(reservedSlot);

        SlotResponse result = slotService.releaseSlot(slotId);

        assertThat(result.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
        assertThat(result.getReservedBy()).isNull();
    }

    @Test
    void releaseSlot_shouldBeIdempotent_whenAlreadyAvailable() {
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(availableSlot));
        when(slotRepository.save(any(DoctorSlot.class))).thenReturn(availableSlot);

        // Should not throw — releasing an available slot is a no-op
        SlotResponse result = slotService.releaseSlot(slotId);
        assertThat(result.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
    }

    @Test
    void getSlotById_shouldThrow_whenNotFound() {
        UUID unknown = UUID.randomUUID();
        when(slotRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotService.getSlotById(unknown))
                .isInstanceOf(SlotNotFoundException.class)
                .hasMessageContaining(unknown.toString());
    }
}
