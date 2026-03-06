package com.example.doctorservice.service;

import com.example.doctorservice.dto.*;
import com.example.doctorservice.exception.DoctorNotFoundException;
import com.example.doctorservice.exception.DuplicateDoctorEmailException;
import com.example.doctorservice.model.Doctor;
import com.example.doctorservice.model.DoctorSlot;
import com.example.doctorservice.model.SlotStatus;
import com.example.doctorservice.repository.DoctorRepository;
import com.example.doctorservice.repository.DoctorSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorSlotRepository slotRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE DOCTOR
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public DoctorResponse createDoctor(DoctorRequest request) {
        log.info("Creating doctor with email: {}", request.getEmail());

        if (doctorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateDoctorEmailException(request.getEmail());
        }

        Doctor doctor = Doctor.builder()
                .userId(request.getUserId())
                .name(request.getName())
                .specialization(request.getSpecialization())
                .email(request.getEmail())
                .phone(request.getPhone())
                .licenseNumber(request.getLicenseNumber())
                .department(request.getDepartment())
                .yearsOfExperience(request.getYearsOfExperience())
                .isActive(true)
                .build();

        return toDoctorResponse(doctorRepository.save(doctor));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET BY ID
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(UUID id) {
        log.debug("Fetching doctor id={}", id);
        return toDoctorResponse(
                doctorRepository.findById(id)
                        .orElseThrow(() -> new DoctorNotFoundException(id)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LIST ALL DOCTORS (optional filter)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllDoctors(String specialization, String department) {
        List<Doctor> doctors;

        if (specialization != null && !specialization.isBlank()) {
            doctors = doctorRepository.findBySpecializationIgnoreCase(specialization);
        } else if (department != null && !department.isBlank()) {
            doctors = doctorRepository.findByDepartmentIgnoreCase(department);
        } else {
            doctors = doctorRepository.findAll();
        }

        return doctors.stream()
                .map(this::toDoctorResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE SLOTS
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public List<SlotResponse> createSlots(UUID doctorId, SlotRequest request) {
        log.info("Creating {} slot(s) for doctor id={}", request.getSlots().size(), doctorId);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException(doctorId));

        List<DoctorSlot> slots = request.getSlots().stream()
                .map(entry -> DoctorSlot.builder()
                        .doctor(doctor)
                        .date(entry.getDate())
                        .startTime(entry.getStartTime())
                        .endTime(entry.getEndTime())
                        .status(SlotStatus.AVAILABLE)
                        .build())
                .collect(Collectors.toList());

        return slotRepository.saveAll(slots).stream()
                .map(this::toSlotResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET SLOTS BY DATE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<SlotResponse> getSlotsByDate(UUID doctorId, LocalDate date) {
        log.debug("Fetching slots for doctor id={} on {}", doctorId, date);

        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException(doctorId);
        }

        return slotRepository
                .findByDoctorIdAndDateOrderByStartTimeAsc(doctorId, date)
                .stream()
                .map(this::toSlotResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mappers
    // ─────────────────────────────────────────────────────────────────────────

    private DoctorResponse toDoctorResponse(Doctor d) {
        return DoctorResponse.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .name(d.getName())
                .specialization(d.getSpecialization())
                .email(d.getEmail())
                .phone(d.getPhone())
                .licenseNumber(d.getLicenseNumber())
                .department(d.getDepartment())
                .yearsOfExperience(d.getYearsOfExperience())
                .isActive(d.isActive())
                .createdAt(d.getCreatedAt())
                .build();
    }

    private SlotResponse toSlotResponse(DoctorSlot s) {
        return SlotResponse.builder()
                .id(s.getId())
                .doctorId(s.getDoctor().getId())
                .doctorName(s.getDoctor().getName())
                .date(s.getDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .status(s.getStatus())
                .reservedBy(s.getReservedBy())
                .appointmentId(s.getAppointmentId())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
