-- One shared table for all tenants: every row carries its tenant_id
CREATE TABLE IF NOT EXISTS metrics (
    id            BIGSERIAL     PRIMARY KEY,
    team_name     VARCHAR(255)  NOT NULL,
    type          VARCHAR(40)   NOT NULL,
    value         INTEGER       NOT NULL,
    recorded_date DATE          NOT NULL,
    notes         VARCHAR(1000),
    created_at    TIMESTAMP     NOT NULL,
    tenant_id     VARCHAR(100)  NOT NULL
);

-- Every query filters by tenant, so the tenant comes first in each index
CREATE INDEX IF NOT EXISTS idx_metrics_tenant_type ON metrics (tenant_id, type);
CREATE INDEX IF NOT EXISTS idx_metrics_tenant_date ON metrics (tenant_id, recorded_date);
