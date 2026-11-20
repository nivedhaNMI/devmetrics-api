package com.nivedha.devmetrics.tenant;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class TenantFilterTest {

    private final TenantFilter filter = new TenantFilter();

    @Test
    void rejectsApiRequestWithoutTenantHeader() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/metrics");
        var response = new MockHttpServletResponse();
        var chainCalled = new AtomicBoolean(false);

        filter.doFilter(request, response, (req, res) -> chainCalled.set(true));

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(chainCalled).isFalse();
    }

    @Test
    void rejectsTenantIdWithInvalidCharacters() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/metrics");
        request.addHeader(TenantFilter.TENANT_HEADER, "team'; DROP TABLE metrics;--");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void setsTenantDuringRequestAndClearsItAfterwards() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/metrics");
        request.addHeader(TenantFilter.TENANT_HEADER, "Team-Alpha");
        var tenantDuringRequest = new AtomicReference<String>();
        FilterChain chain = (req, res) -> tenantDuringRequest.set(TenantContext.getTenant());

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(tenantDuringRequest.get()).isEqualTo("team-alpha");
        assertThat(TenantContext.getTenant()).isNull(); // no leak to the next request on this thread
    }

    @Test
    void monitoringEndpointsDoNotNeedTenantHeader() throws Exception {
        var request = new MockHttpServletRequest("GET", "/actuator/prometheus");
        var response = new MockHttpServletResponse();
        var chainCalled = new AtomicBoolean(false);

        filter.doFilter(request, response, (req, res) -> chainCalled.set(true));

        assertThat(chainCalled).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
