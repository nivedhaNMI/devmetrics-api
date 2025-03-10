package com.nivedha.devmetrics.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "metrics")
@Data
@NoArgsConstructor
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String teamName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetricType type;

    @NotNull
    @Column(nullable = false)
    private Integer value;

    @NotNull
    @Column(nullable = false)
    private LocalDate recordedDate;

    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Tenant isolation — every row belongs to a specific team/tenant
    @Column(nullable = false)
    private String tenantId;

    public enum MetricType {
        DEPLOY_FREQUENCY,
        INCIDENT_COUNT,
        LEAD_TIME_DAYS,
        CHANGE_FAILURE_RATE
    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
