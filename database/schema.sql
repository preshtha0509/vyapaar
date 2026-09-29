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
    last_restocked_date DATE,
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
