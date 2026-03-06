package com.example.doctorservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a doctor profile in the clinic system.
 * <p>
 * {@code userId} is an optional link back to the user-service (same UUID that
 * Supabase Auth assigns). It is NOT enforced as a foreign key because the
 * doctor and user schemas live in different service boundaries.
 */
@Entity
@Table(name = "doctors")   // stored in the "doctor" schema (see application.yml → default_schema)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * Optional reference to the matching user-service UUID.
     * Allows linking a DOCTOR-role user to their doctor profile.
     */
    @Column(name = "user_id")
    private UUID userId;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String specialization;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Column(name = "license_number", unique = true)
    private String licenseNumber;

    private String department;

    @Column(name = "years_of_experience")
    private int yearsOfExperience;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
