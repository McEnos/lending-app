CREATE TABLE LOAN_PRODUCT
(
    id            BIGINT AUTO_INCREMENT  PRIMARY KEY,
    name          VARCHAR(255)   NOT NULL UNIQUE,
    min_amount    DECIMAL(19, 2) NOT NULL,
    max_amount    DECIMAL(19, 2) NOT NULL,
    interest_rate DECIMAL(5, 2)  NOT NULL,
    tenure_type   VARCHAR(50)    NOT NULL,
    min_tenure    INT            NOT NULL,
    max_tenure    INT            NOT NULL
);

CREATE TABLE FEE_CONFIGURATION
(
    id                          BIGINT AUTO_INCREMENT  PRIMARY KEY,
    loan_product_id             BIGINT         NOT NULL,
    fee_type                    VARCHAR(50)    NOT NULL,
    calculation_type            VARCHAR(50)    NOT NULL,
    fee_amount                        DECIMAL(19, 2) NOT NULL,
    application_time            VARCHAR(50),
    days_after_due_for_late_fee INT,
    conditions                  VARCHAR(255),
    FOREIGN KEY (loan_product_id) REFERENCES LOAN_PRODUCT (id)
);

CREATE TABLE LOAN
(
    id                            BIGINT AUTO_INCREMENT  PRIMARY KEY,
    customer_id                   BIGINT         NOT NULL,
    loan_product_id               BIGINT         NOT NULL,
    principal_amount              DECIMAL(19, 2) NOT NULL,
    interest_rate                 DECIMAL(5, 2)  NOT NULL,
    total_repaid_amount           DECIMAL(19, 2) DEFAULT 0.00,
    outstanding_amount            DECIMAL(19, 2) NOT NULL,
    tenure                        INT            NOT NULL,
    tenure_unit                   VARCHAR(50)    NOT NULL,
    origination_date              DATE,
    disbursement_date             DATE,
    final_due_date                DATE,
    status                        VARCHAR(50)    NOT NULL,
    is_installment_loan           BOOLEAN        NOT NULL,
    next_billing_date             DATE,
    consolidated_billing_cycle_id VARCHAR(100),
    FOREIGN KEY (loan_product_id) REFERENCES LOAN_PRODUCT (id)
);
CREATE INDEX idx_loan_customer_id ON LOAN (customer_id);
CREATE INDEX idx_loan_status ON LOAN (status);
CREATE INDEX idx_loan_next_billing_date ON LOAN (next_billing_date);


CREATE TABLE INSTALLMENT
(
    id                  BIGINT  AUTO_INCREMENT  PRIMARY KEY,
    loan_id             BIGINT         NOT NULL,
    installment_number  INT            NOT NULL,
    due_date            DATE           NOT NULL,
    principal_component DECIMAL(19, 2) NOT NULL,
    interest_component  DECIMAL(19, 2) NOT NULL,
    fee_component       DECIMAL(19, 2) DEFAULT 0.00,
    total_amount_due    DECIMAL(19, 2) NOT NULL,
    amount_paid         DECIMAL(19, 2) DEFAULT 0.00,
    status              VARCHAR(50)    NOT NULL,
    payment_date        DATE,
    FOREIGN KEY (loan_id) REFERENCES LOAN (id)
);
CREATE INDEX idx_installment_loan_id ON INSTALLMENT (loan_id);
CREATE INDEX idx_installment_due_date ON INSTALLMENT (due_date);
CREATE INDEX idx_installment_status ON INSTALLMENT (status);


CREATE TABLE APPLIED_FEE
(
    id           BIGINT AUTO_INCREMENT  PRIMARY KEY,
    loan_id      BIGINT         NOT NULL,
    fee_type     VARCHAR(50)    NOT NULL,
    amount       DECIMAL(19, 2) NOT NULL,
    date_applied DATE           NOT NULL,
    reason       VARCHAR(255),
    paid         BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (loan_id) REFERENCES LOAN (id)
);
CREATE INDEX idx_applied_fee_loan_id ON APPLIED_FEE (loan_id);


CREATE TABLE REPAYMENT
(
    id                    BIGINT  AUTO_INCREMENT PRIMARY KEY,
    loan_id               BIGINT         NOT NULL,
    installment_id        BIGINT,
    amount                DECIMAL(19, 2) NOT NULL,
    payment_date_time     TIMESTAMP      NOT NULL,
    payment_method        VARCHAR(100),
    transaction_reference VARCHAR(255),
    FOREIGN KEY (loan_id) REFERENCES LOAN (id),
    FOREIGN KEY (installment_id) REFERENCES INSTALLMENT (id)
);
CREATE INDEX idx_repayment_loan_id ON REPAYMENT (loan_id);