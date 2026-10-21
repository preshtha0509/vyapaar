-- VYAPAAR reference schema.
--
-- NOT the source of truth. Hibernate owns the live schema through ddl-auto: update, which compares
-- the @Entity classes to the database at every startup and issues the DDL itself. This file exists
-- so the shape can be read without starting the app, and so a database can be created by hand.
--
-- Keep it in step with the entities. It was out of date before: products had gained
-- stock_quantity and active, and the three Module 15 receipt tables were missing entirely.

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    city VARCHAR(255) NOT NULL,
    opening_balance DOUBLE NOT NULL,
    gst_number VARCHAR(255),
    notes VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    city VARCHAR(255) NOT NULL,
    opening_balance DOUBLE NOT NULL,
    gst_number VARCHAR(255),
    notes VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    unit ENUM('KG', 'QUINTAL', 'BAG') NOT NULL,
    stock_quantity DOUBLE NOT NULL DEFAULT 0,
    last_restocked_date DATE,
    active BIT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS purchases (
    id BIGINT NOT NULL AUTO_INCREMENT,
    supplier_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity DOUBLE NOT NULL,
    rate DOUBLE NOT NULL,
    total DOUBLE NOT NULL,
    purchase_date DATE NOT NULL,
    payment_status ENUM('PAID', 'PENDING') NOT NULL,
    notes VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_purchases_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers (id),
    CONSTRAINT fk_purchases_product
        FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE IF NOT EXISTS sales (
    id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity DOUBLE NOT NULL,
    rate DOUBLE NOT NULL,
    total DOUBLE NOT NULL,
    sale_date DATE NOT NULL,
    payment_status ENUM('CASH', 'UDHARI') NOT NULL,
    notes VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_sales_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_sales_product
        FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    party_type ENUM('CUSTOMER', 'SUPPLIER') NOT NULL,
    party_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    payment_date DATE NOT NULL,
    mode ENUM('CASH', 'UPI', 'BANK_TRANSFER', 'CHEQUE') NOT NULL,
    notes VARCHAR(255),
    PRIMARY KEY (id)
);
-- party_id deliberately has NO foreign key: it points into customers OR suppliers depending on
-- party_type, and a foreign key must name exactly one target table. The application enforces
-- that reference; the database cannot.

-- ---------------------------------------------------------------------------
-- Module 15: receipts. Added by ddl-auto on the receipts branch.
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS receipts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    receipt_date DATE NOT NULL,
    receipt_number VARCHAR(40) NOT NULL,
    public_token VARCHAR(64) NOT NULL,
    subtotal DOUBLE NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_receipts_number UNIQUE (receipt_number),
    CONSTRAINT uk_receipts_token UNIQUE (public_token),
    CONSTRAINT uk_receipts_customer_date UNIQUE (customer_id, receipt_date)
);

CREATE TABLE IF NOT EXISTS receipt_lines (
    id BIGINT NOT NULL AUTO_INCREMENT,
    receipt_id BIGINT NOT NULL,
    sale_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    quantity DOUBLE NOT NULL,
    rate DOUBLE NOT NULL,
    line_total DOUBLE NOT NULL,
    PRIMARY KEY (id)
);
-- product_name and unit are stored as text on purpose: a receipt already given to a customer must
-- not change if the product is later renamed or soft-deleted.

CREATE TABLE IF NOT EXISTS receipt_sends (
    id BIGINT NOT NULL AUTO_INCREMENT,
    receipt_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(40),
    status VARCHAR(20) NOT NULL,
    error VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
);
-- Append-only audit. status OPENED means the owner tapped through to WhatsApp; it does NOT mean
-- the customer received anything.

-- ---------------------------------------------------------------------------
-- Views are NOT created by Hibernate. Run database/chat_views.sql by hand,
-- once per database, for v_customer_balance and v_supplier_balance.
-- ---------------------------------------------------------------------------
