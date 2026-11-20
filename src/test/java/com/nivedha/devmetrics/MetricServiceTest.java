package com.nivedha.devmetrics;

import com.nivedha.devmetrics.model.Metric;
import com.nivedha.devmetrics.repository.MetricRepository;
import com.nivedha.devmetrics.service.MetricService;
import com.nivedha.devmetrics.tenant.TenantContext;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricServiceTest {

    @Mock
    private MetricRepository metricRepository;

    private SimpleMeterRegistry meterRegistry;
    private MetricService metricService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricService = new MetricService(metricRepository, meterRegistry);
        TenantContext.setTenant("team-alpha");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private Metric deployFrequency(int value) {
        Metric metric = new Metric();
        metric.setTeamName("Backend Team");
        metric.setType(Metric.MetricType.DEPLOY_FREQUENCY);
        metric.setValue(value);
        metric.setRecordedDate(LocalDate.now());
        return metric;
    }

    @Test
    void shouldRecordMetricWithCorrectTenant() {
        when(metricRepository.save(any(Metric.class))).thenAnswer(inv -> {
            Metric m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        Metric saved = metricService.recordMetric(deployFrequency(5));

        assertThat(saved.getTenantId()).isEqualTo("team-alpha");
        assertThat(saved.getType()).isEqualTo(Metric.MetricType.DEPLOY_FREQUENCY);
        verify(metricRepository, times(1)).save(any());
    }

    @Test
    void shouldCountRecordedMetricsPerTenantForMonitoring() {
        when(metricRepository.save(any(Metric.class))).thenAnswer(inv -> inv.getArgument(0));

        metricService.recordMetric(deployFrequency(5));
        metricService.recordMetric(deployFrequency(7));

        double count = meterRegistry.get("devmetrics.metrics.recorded")
                .tag("tenant", "team-alpha")
                .tag("type", "DEPLOY_FREQUENCY")
                .counter()
                .count();
        assertThat(count).isEqualTo(2.0);
    }

    @Test
    void shouldOnlyReturnMetricsForCurrentTenant() {
        Metric m1 = new Metric();
        m1.setTenantId("team-alpha");
        m1.setType(Metric.MetricType.INCIDENT_COUNT);

        when(metricRepository.findByTenantId("team-alpha")).thenReturn(List.of(m1));

        List<Metric> results = metricService.getAllMetrics();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTenantId()).isEqualTo("team-alpha");
    }
}
