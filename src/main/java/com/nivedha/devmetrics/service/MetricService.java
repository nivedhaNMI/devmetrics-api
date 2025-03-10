package com.nivedha.devmetrics.service;

import com.nivedha.devmetrics.model.Metric;
import com.nivedha.devmetrics.repository.MetricRepository;
import com.nivedha.devmetrics.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricService {

    private final MetricRepository metricRepository;

    public Metric recordMetric(Metric metric) {
        String tenant = TenantContext.getTenant();
        metric.setTenantId(tenant);
        log.info("Recording {} metric for tenant {}", metric.getType(), tenant);
        return metricRepository.save(metric);
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
