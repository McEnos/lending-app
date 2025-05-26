INSERT INTO CUSTOMERS (id, first_name, last_name, email, phone_number, financial_summary, current_loan_limit,
                       created_at, updated_at)
VALUES (1, 'John', 'Doe', 'john.doe@example.com', '+1-555-0101', 'Good credit standing, stable income.', 5000.00,
        PARSEDATETIME('2023-01-15 10:00:00', 'yyyy-MM-dd HH:mm:ss'),
        PARSEDATETIME('2023-05-20 11:30:00', 'yyyy-MM-dd HH:mm:ss'));

INSERT INTO LOAN_LIMIT_HISTORY (customer_id, previous_limit, new_limit, change_timestamp, reason, changed_by)
VALUES (1, 0.00, 3000.00, PARSEDATETIME('2023-01-15 10:00:00', 'yyyy-MM-dd HH:mm:ss'),
        'Initial loan limit assigned upon customer creation.', 'SYSTEM'),
       (1, 3000.00, 5000.00, PARSEDATETIME('2023-05-20 11:30:00', 'yyyy-MM-dd HH:mm:ss'),
        'Increased limit due to consistent on-time repayments on other products.', 'RISK_ANALYSIS_SYSTEM');


INSERT INTO CUSTOMERS (id, first_name, last_name, email, phone_number, financial_summary, current_loan_limit,
                       created_at, updated_at)
VALUES (2, 'Jane', 'Smith', 'jane.smith@example.com', '+44-20-7946-0102', 'Fair credit, improving repayment history.',
        2500.00,
        PARSEDATETIME('2023-03-10 14:15:00', 'yyyy-MM-dd HH:mm:ss'),
        PARSEDATETIME('2023-03-10 14:15:00', 'yyyy-MM-dd HH:mm:ss'));

INSERT INTO LOAN_LIMIT_HISTORY (customer_id, previous_limit, new_limit, change_timestamp, reason, changed_by)
VALUES (2, 0.00, 2500.00, PARSEDATETIME('2023-03-10 14:15:00', 'yyyy-MM-dd HH:mm:ss'),
        'Initial loan limit assigned upon customer creation.', 'SYSTEM');


INSERT INTO CUSTOMERS (id, first_name, last_name, email, phone_number, financial_summary, current_loan_limit,
                       created_at, updated_at)
VALUES (3, 'Alice', 'Brown', 'alice.brown@example.com', '+1-555-0103', 'New customer, limited credit history.', 1000.00,
        PARSEDATETIME('2023-11-01 09:00:00', 'yyyy-MM-dd HH:mm:ss'),
        PARSEDATETIME('2023-11-01 09:00:00', 'yyyy-MM-dd HH:mm:ss'));

INSERT INTO LOAN_LIMIT_HISTORY (customer_id, previous_limit, new_limit, change_timestamp, reason, changed_by)
VALUES (3, 0.00, 1000.00, PARSEDATETIME('2023-11-01 09:00:00', 'yyyy-MM-dd HH:mm:ss'),
        'Initial loan limit assigned upon customer creation.', 'SYSTEM');