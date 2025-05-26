CREATE TABLE CUSTOMERS
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name         VARCHAR(100)   NOT NULL,
    last_name          VARCHAR(100)   NOT NULL,
    email              VARCHAR(150)   NOT NULL UNIQUE,
    phone_number       VARCHAR(30)    NOT NULL,
    financial_summary  TEXT,
    current_loan_limit DECIMAL(19, 2) NOT NULL,
    created_at         TIMESTAMP      NOT NULL,
    updated_at         TIMESTAMP      NOT NULL
);

CREATE INDEX idx_customer_email ON CUSTOMERS (email);
CREATE INDEX idx_customer_phone_number ON CUSTOMERS (phone_number);


CREATE TABLE LOAN_LIMIT_HISTORY
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id      BIGINT         NOT NULL,
    previous_limit   DECIMAL(19, 2) NOT NULL,
    new_limit        DECIMAL(19, 2) NOT NULL,
    change_timestamp TIMESTAMP      NOT NULL,
    reason           VARCHAR(500)   NOT NULL,
    changed_by       VARCHAR(100),
    FOREIGN KEY (customer_id) REFERENCES CUSTOMERS (id) ON DELETE CASCADE
);

CREATE INDEX idx_llh_customer_id_timestamp ON LOAN_LIMIT_HISTORY (customer_id, change_timestamp DESC);