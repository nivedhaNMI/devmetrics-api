package com.nivedha.devmetrics.tenant;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class TenantFilter implements Filter {

    public static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String tenantId = request.getHeader(TENANT_HEADER);

        if (tenantId == null || tenantId.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing required header: X-Tenant-ID");
            return;
        }

        // Sanitise tenant ID — only allow alphanumeric and hyphens
        if (!tenantId.matches("^[a-zA-Z0-9\\-]+$")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid tenant ID format.");
            return;
        }

        try {
            TenantContext.setTenant(tenantId.toLowerCase());
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear();
        }
    }
}
