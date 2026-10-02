-- V1__initial_schema.sql: Smart Service Full Platform Schema

-- 1. USERS & SECURITY
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'STAFF', 'TECHNICIAN', 'CUSTOMER')),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- REFRESH TOKENS
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens(token_hash);

-- 2. CUSTOMERS
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    address TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customers_user ON customers(user_id);
CREATE INDEX idx_customers_phone ON customers(phone);
CREATE INDEX idx_customers_email ON customers(email);

-- 3. TECHNICIANS
CREATE TABLE technicians (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    specialization VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'BUSY', 'OFFLINE')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_technicians_user ON technicians(user_id);

-- 4. DEVICES
CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    serial_number VARCHAR(100),
    imei VARCHAR(100),
    device_type VARCHAR(50) NOT NULL, -- Mobile, Laptop, Tablet, Desktop, Smartwatch, Other
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_devices_customer ON devices(customer_id);
CREATE INDEX idx_devices_serial ON devices(serial_number);
CREATE INDEX idx_devices_imei ON devices(imei);

-- 5. SERVICE REQUESTS
CREATE TABLE service_requests (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    device_id BIGINT NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    problem_title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    status VARCHAR(50) NOT NULL DEFAULT 'REQUESTED' CHECK (status IN ('REQUESTED', 'ACCEPTED', 'REJECTED', 'CONVERTED_TO_JOB', 'CANCELLED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_service_requests_customer ON service_requests(customer_id);
CREATE INDEX idx_service_requests_status ON service_requests(status);

-- 6. REPAIR JOBS
CREATE TABLE repair_jobs (
    id BIGSERIAL PRIMARY KEY,
    job_number VARCHAR(50) NOT NULL UNIQUE,
    service_request_id BIGINT REFERENCES service_requests(id) ON DELETE SET NULL,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    device_id BIGINT NOT NULL REFERENCES devices(id),
    technician_id BIGINT REFERENCES technicians(id),
    status VARCHAR(50) NOT NULL DEFAULT 'REQUESTED',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    labor_cost DECIMAL(10,2) DEFAULT 0.00,
    total_estimated_cost DECIMAL(10,2) DEFAULT 0.00,
    total_actual_cost DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_repair_jobs_job_number ON repair_jobs(job_number);
CREATE INDEX idx_repair_jobs_customer ON repair_jobs(customer_id);
CREATE INDEX idx_repair_jobs_technician ON repair_jobs(technician_id);
CREATE INDEX idx_repair_jobs_status ON repair_jobs(status);

-- REPAIR JOB STATUS HISTORY
CREATE TABLE repair_job_status_history (
    id BIGSERIAL PRIMARY KEY,
    repair_job_id BIGINT NOT NULL REFERENCES repair_jobs(id) ON DELETE CASCADE,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by_user_id BIGINT REFERENCES users(id),
    remarks TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_status_history_job ON repair_job_status_history(repair_job_id);

-- 7. DIAGNOSES
CREATE TABLE diagnoses (
    id BIGSERIAL PRIMARY KEY,
    repair_job_id BIGINT NOT NULL UNIQUE REFERENCES repair_jobs(id) ON DELETE CASCADE,
    technician_id BIGINT NOT NULL REFERENCES technicians(id),
    symptoms TEXT,
    findings TEXT NOT NULL,
    recommended_repair TEXT,
    estimated_labor_cost DECIMAL(10,2) DEFAULT 0.00,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. PARTS & SUPPLIERS & INVENTORY
CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    address TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE parts (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    compatible_device VARCHAR(255),
    purchase_price DECIMAL(10,2) NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    quantity_in_stock INT NOT NULL DEFAULT 0 CHECK (quantity_in_stock >= 0),
    reserved_quantity INT NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    reorder_level INT NOT NULL DEFAULT 5,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_parts_sku ON parts(sku);
CREATE INDEX idx_parts_category ON parts(category);

CREATE TABLE inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    part_id BIGINT NOT NULL REFERENCES parts(id),
    transaction_type VARCHAR(50) NOT NULL CHECK (transaction_type IN ('PURCHASE', 'RESERVATION', 'RELEASE', 'USED', 'RETURN', 'ADJUSTMENT')),
    quantity INT NOT NULL,
    reference_id VARCHAR(100),
    reference_type VARCHAR(50),
    notes TEXT,
    created_by_user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inventory_reservations (
    id BIGSERIAL PRIMARY KEY,
    part_id BIGINT NOT NULL REFERENCES parts(id),
    repair_job_id BIGINT NOT NULL REFERENCES repair_jobs(id) ON DELETE CASCADE,
    quantity INT NOT NULL CHECK (quantity > 0),
    status VARCHAR(50) NOT NULL DEFAULT 'RESERVED' CHECK (status IN ('RESERVED', 'CONSUMED', 'RELEASED')),
    reserved_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. ESTIMATES & ITEMS
CREATE TABLE estimates (
    id BIGSERIAL PRIMARY KEY,
    estimate_number VARCHAR(50) NOT NULL UNIQUE,
    repair_job_id BIGINT NOT NULL REFERENCES repair_jobs(id) ON DELETE CASCADE,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'SENT', 'APPROVED', 'REJECTED')),
    total_parts_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    labor_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    rejection_reason TEXT,
    approved_at TIMESTAMP WITH TIME ZONE,
    rejected_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE estimate_items (
    id BIGSERIAL PRIMARY KEY,
    estimate_id BIGINT NOT NULL REFERENCES estimates(id) ON DELETE CASCADE,
    part_id BIGINT REFERENCES parts(id),
    item_description VARCHAR(255) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL
);

-- 10. INVOICES & PAYMENTS
CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    repair_job_id BIGINT NOT NULL REFERENCES repair_jobs(id),
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PARTIALLY_PAID', 'PAID', 'FAILED', 'REFUNDED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE invoice_items (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description VARCHAR(255) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    item_type VARCHAR(50) NOT NULL DEFAULT 'PART' CHECK (item_type IN ('PART', 'LABOR', 'SERVICE', 'TAX'))
);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoices(id),
    payment_gateway_order_id VARCHAR(100),
    payment_gateway_payment_id VARCHAR(100),
    payment_gateway_signature VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL DEFAULT 'ONLINE',
    payment_status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',
    verified_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 11. NOTIFICATIONS & AUDIT LOGS
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    read_status BOOLEAN NOT NULL DEFAULT FALSE,
    metadata_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    user_email VARCHAR(255),
    role VARCHAR(50),
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    ip_address VARCHAR(100),
    details_json TEXT,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- SEED SEED DATA
-- Default BCrypt Password hash for "Password@123":
INSERT INTO users (email, password_hash, full_name, phone, role, status) VALUES
('admin@smartservice.com', '$2a$10$3D8C7VD0nqAXpexkD0SptuuFl.5GAgvdYPXSk.wDZebe5cD.pnhk.', 'System Administrator', '+18005550199', 'ADMIN', 'ACTIVE'),
('manager@smartservice.com', '$2a$10$3D8C7VD0nqAXpexkD0SptuuFl.5GAgvdYPXSk.wDZebe5cD.pnhk.', 'Service Manager', '+18005550198', 'MANAGER', 'ACTIVE'),
('tech.alex@smartservice.com', '$2a$10$3D8C7VD0nqAXpexkD0SptuuFl.5GAgvdYPXSk.wDZebe5cD.pnhk.', 'Alex Technician', '+18005550197', 'TECHNICIAN', 'ACTIVE'),
('staff@smartservice.com', '$2a$10$3D8C7VD0nqAXpexkD0SptuuFl.5GAgvdYPXSk.wDZebe5cD.pnhk.', 'Front Desk Staff', '+18005550196', 'STAFF', 'ACTIVE'),
('customer.john@gmail.com', '$2a$10$3D8C7VD0nqAXpexkD0SptuuFl.5GAgvdYPXSk.wDZebe5cD.pnhk.', 'John Doe Customer', '+18005550195', 'CUSTOMER', 'ACTIVE');

INSERT INTO customers (user_id, name, email, phone, address) VALUES
(5, 'John Doe Customer', 'customer.john@gmail.com', '+18005550195', '742 Evergreen Terrace, Springfield');

INSERT INTO technicians (user_id, name, email, phone, specialization, status) VALUES
(3, 'Alex Technician', 'tech.alex@smartservice.com', '+18005550197', 'Mobile & Laptop Logic Boards', 'AVAILABLE');

INSERT INTO devices (customer_id, brand, model, serial_number, imei, device_type) VALUES
(1, 'Apple', 'iPhone 15 Pro', 'SN-IPH15-998822', '356891100223344', 'Mobile'),
(1, 'Dell', 'XPS 15 9530', 'SN-DELL-XPS-7744', NULL, 'Laptop');

INSERT INTO parts (sku, name, category, compatible_device, purchase_price, selling_price, quantity_in_stock, reserved_quantity, reorder_level) VALUES
('PART-IPH15-PORT', 'iPhone 15 Charging Port Assembly', 'Charging Port', 'iPhone 15 / 15 Pro', 350.00, 800.00, 25, 0, 5),
('PART-IPH15-BATT', 'iPhone 15 High Capacity Battery', 'Battery', 'iPhone 15 Pro', 450.00, 950.00, 15, 0, 5),
('PART-DELL-RAM16', 'DDR5 16GB 4800MHz Laptop RAM', 'RAM', 'Universal Laptop', 600.00, 1200.00, 30, 0, 8),
('PART-OLED-DISP', 'iPhone 15 OLED Display Original', 'Screen', 'iPhone 15', 2500.00, 4200.00, 10, 0, 3);


