package com.example.doctorservice.controller;

import com.example.doctorservice.client.UserServiceClient;
import com.example.doctorservice.dto.ReserveSlotRequest;
import com.example.doctorservice.dto.SlotResponse;
import com.example.doctorservice.dto.TokenValidationResponse;
import com.example.doctorservice.exception.UnauthorizedException;
import com.example.doctorservice.service.SlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for slot-level operations (reserve / release).
 *
 * <pre>
 * GET    /api/slots/{slotId}          → get slot details     (any authenticated)
 * POST   /api/slots/{slotId}/reserve  → reserve slot         (any authenticated — called by appointment-service)
 * POST   /api/slots/{slotId}/release  → release slot         (any authenticated — saga compensation)
 * </pre>
 *
 * Saga integration:
 * <ol>
 *   <li>appointment-service calls POST /reserve when creating a booking.</li>
 *   <li>If a downstream step fails (payment declined), appointment-service
 *       calls POST /release to roll back the reservation — this is the
 *       <em>compensating transaction</em> in the saga.</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;
    private final UserServiceClient userServiceClient;

    // ── GET /api/slots/{slotId} ────────────────────────────────────────────

    @GetMapping("/{slotId}")
    public ResponseEntity<SlotResponse> getSlot(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID slotId) {

        requireAuthenticated(authHeader);
        return ResponseEntity.ok(slotService.getSlotById(slotId));
    }

    // ── POST /api/slots/{slotId}/reserve ──────────────────────────────────

    @PostMapping("/{slotId}/reserve")
    public ResponseEntity<SlotResponse> reserveSlot(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID slotId,
            @Valid @RequestBody ReserveSlotRequest request) {

        requireAuthenticated(authHeader);
        return ResponseEntity.ok(slotService.reserveSlot(slotId, request));
    }

    // ── POST /api/slots/{slotId}/release ──────────────────────────────────

    @PostMapping("/{slotId}/release")
    public ResponseEntity<SlotResponse> releaseSlot(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID slotId) {

        requireAuthenticated(authHeader);
        return ResponseEntity.ok(slotService.releaseSlot(slotId));
    }

    // ── Auth helper ────────────────────────────────────────────────────────

    private TokenValidationResponse requireAuthenticated(String authHeader) {
        TokenValidationResponse auth = userServiceClient.validateToken(stripBearer(authHeader));
        if (!auth.isValid()) {
            throw new UnauthorizedException("Invalid or expired token");
        }
        return auth;
    }

    private String stripBearer(String header) {
        return header != null && header.startsWith("Bearer ")
                ? header.substring(7)
                : header;
    }
}
