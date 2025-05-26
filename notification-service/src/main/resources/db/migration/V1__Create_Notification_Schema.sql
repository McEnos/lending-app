CREATE TABLE NOTIFICATION_TEMPLATES
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_code    VARCHAR(100) NOT NULL UNIQUE,
    subject_template VARCHAR(500) NOT NULL,
    body_template    TEXT         NOT NULL,
    default_channel  VARCHAR(50)  NOT NULL,
    language_code    VARCHAR(10) DEFAULT 'en-US',
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL
);

CREATE INDEX idx_nt_template_code ON NOTIFICATION_TEMPLATES (template_code);


CREATE TABLE NOTIFICATION_LOGS
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id       BIGINT,
    recipient_address VARCHAR(255),
    channel           VARCHAR(50) NOT NULL,
    template_id       BIGINT,
    event_type        VARCHAR(100),
    subject           VARCHAR(500),
    body              TEXT,
    status            VARCHAR(50) NOT NULL,
    scheduled_at      TIMESTAMP,
    sent_at           TIMESTAMP,
    processed_at      TIMESTAMP   NOT NULL,
    failure_reason    VARCHAR(1000),
    FOREIGN KEY (template_id) REFERENCES NOTIFICATION_TEMPLATES (id)
);

CREATE INDEX idx_nl_customer_id ON NOTIFICATION_LOGS (customer_id);
CREATE INDEX idx_nl_status ON NOTIFICATION_LOGS (status);
CREATE INDEX idx_nl_event_type ON NOTIFICATION_LOGS (event_type);
CREATE INDEX idx_nl_processed_at ON NOTIFICATION_LOGS (processed_at DESC);


CREATE TABLE NOTIFICATION_LOG_PARAMETERS
(
    notification_log_id BIGINT       NOT NULL,
    parameter_name      VARCHAR(100) NOT NULL,
    parameter_value     VARCHAR(1000),
    PRIMARY KEY (notification_log_id, parameter_name),
    FOREIGN KEY (notification_log_id) REFERENCES NOTIFICATION_LOGS (id) ON DELETE CASCADE
);