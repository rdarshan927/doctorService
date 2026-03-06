package com.example.doctorservice.controller;

import com.example.doctorservice.client.UserServiceClient;
import com.example.doctorservice.dto.*;
import com.example.doctorservice.exception.ForbiddenException;
import com.example.doctorservice.exception.UnauthorizedException;
import com.example.doctorservice.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for Doctor & Schedule endpoints.
 *
 * <pre>
 * POST   /api/doctors                           → create doctor        (ADMIN, RECEPTIONIST)
 * GET    /api/doctors                           → list all doctors     (any authenticated)
 * GET    /api/doctors/{id}                      → get doctor           (any authenticated)
 * POST   /api/doctors/{id}/slots               → create slots         (ADMIN, DOCTOR, RECEPTIONIST)
 * GET    /api/doctors/{id}/slots?date=YYYY-MM-DD → get slots by date  (any authenticated)
 * </pre>
 *
 * Token validation is delegated to the user-service via {@link UserServiceClient}.
 * This is consistent with the "token introspection" pattern used by all
 * microservices in this clinic system.
 */
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final UserServiceClient userServiceClient;

    // ── POST /api/doctors ──────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<DoctorResponse> createDoctor(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody DoctorRequest request) {

        requireRole(authHeader, "ADMIN", "RECEPTIONIST", "DOCTOR");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(doctorService.createDoctor(request));
    }

    // ── PUT /api/doctors/{id} (full update + optional verify) ─────────────

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @RequestBody DoctorRequest request) {

        requireRole(authHeader, "ADMIN", "RECEPTIONIST");
        return ResponseEntity.ok(doctorService.updateDoctor(id, request));
    }

    // ── PATCH /api/doctors/{id}/verify (toggle verified only) ─────────────

    @PatchMapping("/{id}/verify")
    public ResponseEntity<DoctorResponse> verifyDoctor(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @RequestBody VerifyRequest request) {

        requireRole(authHeader, "ADMIN", "RECEPTIONIST");
        return ResponseEntity.ok(doctorService.verifyDoctor(id, request.isVerified()));
    }

    // ── GET /api/doctors ───────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> getAllDoctors(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String department) {

        requireAuthenticated(authHeader);
        return ResponseEntity.ok(doctorService.getAllDoctors(specialization, department));
    }

    // ── GET /api/doctors/{id} ──────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id) {

        requireAuthenticated(authHeader);
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    // ── POST /api/doctors/{id}/slots ───────────────────────────────────────

    @PostMapping("/{id}/slots")
    public ResponseEntity<List<SlotResponse>> createSlots(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @Valid @RequestBody SlotRequest request) {

        requireRole(authHeader, "ADMIN", "DOCTOR", "RECEPTIONIST");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(doctorService.createSlots(id, request));
    }

    // ── GET /api/doctors/{id}/slots?date=YYYY-MM-DD ────────────────────────

    @GetMapping("/{id}/slots")
    public ResponseEntity<List<SlotResponse>> getSlotsByDate(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        requireAuthenticated(authHeader);
        return ResponseEntity.ok(doctorService.getSlotsByDate(id, date));
    }

    // ── PATCH /api/doctors/{id}/link-user ─────────────────────────────────

    @PatchMapping("/{id}/link-user")
    public ResponseEntity<DoctorResponse> linkUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @RequestBody LinkUserRequest request) {

        requireRole(authHeader, "ADMIN", "RECEPTIONIST");
        return ResponseEntity.ok(doctorService.linkUser(id, request.getUserId()));
    }

    // ── Auth helpers ───────────────────────────────────────────────────────

    private TokenValidationResponse requireAuthenticated(String authHeader) {
        TokenValidationResponse auth = userServiceClient.validateToken(stripBearer(authHeader));
        if (!auth.isValid()) {
            throw new UnauthorizedException("Invalid or expired token");
        }
        return auth;
    }

    private TokenValidationResponse requireRole(String authHeader, String... roles) {
        TokenValidationResponse auth = requireAuthenticated(authHeader);
        String userRole = auth.getRole();
        boolean hasRole = Arrays.stream(roles)
                .anyMatch(r -> r.equalsIgnoreCase(userRole));
        if (!hasRole) {
            throw new ForbiddenException(
                    "Insufficient permissions. Required one of: " + String.join(", ", roles));
        }
        return auth;
    }

    private String stripBearer(String header) {
        return header != null && header.startsWith("Bearer ")
                ? header.substring(7)
                : header;
    }
}
