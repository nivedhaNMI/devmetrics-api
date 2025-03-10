package com.nivedha.devmetrics.controller;

import com.nivedha.devmetrics.model.Metric;
import com.nivedha.devmetrics.service.MetricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Tag(name = "Metrics", description = "Engineering delivery metrics — tenant-scoped via X-Tenant-ID header")
public class MetricController {

    private final MetricService metricService;

    @PostMapping
    @Operation(summary = "Record a new metric for the current tenant")
    public ResponseEntity<Metric> recordMetric(@Valid @RequestBody Metric metric) {
        return ResponseEntity.status(HttpStatus.CREATED).body(metricService.recordMetric(metric));
    }

    @GetMapping
    @Operation(summary = "Get all metrics for the current tenant")
    public ResponseEntity<List<Metric>> getAllMetrics() {
        return ResponseEntity.ok(metricService.getAllMetrics());
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get metrics filtered by type")
    public ResponseEntity<List<Metric>> getByType(@PathVariable Metric.MetricType type) {
        return ResponseEntity.ok(metricService.getMetricsByType(type));
    }

    @GetMapping("/range")
    @Operation(summary = "Get metrics within a date range")
    public ResponseEntity<List<Metric>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Start date (YYYY-MM-DD)") LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "End date (YYYY-MM-DD)") LocalDate to) {
        return ResponseEntity.ok(metricService.getMetricsByDateRange(from, to));
    }

    @GetMapping("/average/{type}")
    @Operation(summary = "Get average value for a metric type")
    public ResponseEntity<Map<String, Object>> getAverage(@PathVariable Metric.MetricType type) {
        Double avg = metricService.getAverage(type);
        return ResponseEntity.ok(Map.of("type", type, "average", avg != null ? avg : 0));
    }
}
