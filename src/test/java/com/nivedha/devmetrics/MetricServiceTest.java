package com.nivedha.devmetrics;

import com.nivedha.devmetrics.model.Metric;
import com.nivedha.devmetrics.repository.MetricRepository;
import com.nivedha.devmetrics.service.MetricService;
import com.nivedha.devmetrics.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private MetricService metricService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenant("team-alpha");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldRecordMetricWithCorrectTenant() {
        Metric metric = new Metric();
        metric.setTeamName("Backend Team");
        metric.setType(Metric.MetricType.DEPLOY_FREQUENCY);
        metric.setValue(5);
        metric.setRecordedDate(LocalDate.now());

        when(metricRepository.save(any(Metric.class))).thenAnswer(inv -> {
            Metric m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        Metric saved = metricService.recordMetric(metric);

        assertThat(saved.getTenantId()).isEqualTo("team-alpha");
        assertThat(saved.getType()).isEqualTo(Metric.MetricType.DEPLOY_FREQUENCY);
        verify(metricRepository, times(1)).save(any());
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
