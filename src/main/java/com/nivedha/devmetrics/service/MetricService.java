package com.nivedha.devmetrics.service;

import com.nivedha.devmetrics.model.Metric;
import com.nivedha.devmetrics.repository.MetricRepository;
import com.nivedha.devmetrics.tenant.TenantContext;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricService {

    static final String RECORDED_COUNTER = "devmetrics.metrics.recorded";

    private final MetricRepository metricRepository;
    private final MeterRegistry meterRegistry;

    public Metric recordMetric(Metric metric) {
        String tenant = TenantContext.getTenant();
        metric.setTenantId(tenant);
        log.info("Recording {} metric for tenant {}", metric.getType(), tenant);
        Metric saved = metricRepository.save(metric);

        // Business metric for Prometheus/Grafana: how many metrics each tenant records, per type
        meterRegistry.counter(RECORDED_COUNTER, "tenant", tenant, "type", metric.getType().name()).increment();
        return saved;
    }

    public List<Metric> getAllMetrics() {
        return metricRepository.findByTenantId(TenantContext.getTenant());
    }

    public List<Metric> getMetricsByType(Metric.MetricType type) {
        return metricRepository.findByTenantIdAndType(TenantContext.getTenant(), type);
    }

    public List<Metric> getMetricsByDateRange(LocalDate from, LocalDate to) {
        return metricRepository.findByTenantIdAndRecordedDateBetween(TenantContext.getTenant(), from, to);
    }

    public Double getAverage(Metric.MetricType type) {
        return metricRepository.findAverageByTenantAndType(TenantContext.getTenant(), type);
    }
}
