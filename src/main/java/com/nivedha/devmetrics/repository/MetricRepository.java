package com.nivedha.devmetrics.repository;

import com.nivedha.devmetrics.model.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface MetricRepository extends JpaRepository<Metric, Long> {

    // All queries are tenant-scoped — no tenant can ever see another tenant's data
    List<Metric> findByTenantId(String tenantId);

    List<Metric> findByTenantIdAndType(String tenantId, Metric.MetricType type);

    List<Metric> findByTenantIdAndRecordedDateBetween(String tenantId, LocalDate from, LocalDate to);

    @Query("SELECT AVG(m.value) FROM Metric m WHERE m.tenantId = :tenantId AND m.type = :type")
    Double findAverageByTenantAndType(String tenantId, Metric.MetricType type);
}
