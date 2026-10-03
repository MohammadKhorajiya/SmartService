-- Migration V2: Add missing performance indexes on high-frequency query columns
CREATE INDEX IF NOT EXISTS idx_service_requests_status ON service_requests(status);
CREATE INDEX IF NOT EXISTS idx_repair_jobs_status ON repair_jobs(status);
CREATE INDEX IF NOT EXISTS idx_devices_customer_id ON devices(customer_id);
