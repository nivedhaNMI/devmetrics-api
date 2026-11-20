package com.nivedha.devmetrics.dto;

import com.nivedha.devmetrics.model.Metric;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * What a client may send when recording a metric.
 * id, tenantId and createdAt are set by the server only, so a client
 * can never overwrite an existing row or write into another tenant.
 */
public record RecordMetricRequest(
        @NotBlank @Size(max = 255) String teamName,
        @NotNull Metric.MetricType type,
        @NotNull @PositiveOrZero Integer value,
        @NotNull LocalDate recordedDate,
        @Size(max = 1000) String notes
) {}
