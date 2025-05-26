INSERT INTO LOAN_PRODUCT (id, name, min_amount, max_amount, interest_rate, tenure_type, min_tenure, max_tenure)
VALUES (1, 'Quick Cash - 30 Days', 100.00, 1000.00, 25.0, 'DAYS', 30, 30),
       (2, 'Flexi Loan - 60 to 90 Days', 200.00, 1500.00, 20.0, 'DAYS', 60, 90);

INSERT INTO FEE_CONFIGURATION (loan_product_id, fee_type, calculation_type, fee_amount, application_time,
                               days_after_due_for_late_fee, conditions)
VALUES (1, 'SERVICE_FEE', 'FIXED', 15.00, 'ORIGINATION', NULL, NULL),
       (1, 'LATE_FEE', 'FIXED', 30.00, 'CONDITIONAL', 3, NULL);

INSERT INTO FEE_CONFIGURATION (loan_product_id, fee_type, calculation_type, fee_amount, application_time,
                               days_after_due_for_late_fee, conditions)
VALUES (2, 'SERVICE_FEE', 'PERCENTAGE', 2.5, 'ORIGINATION', NULL, NULL),
       (2, 'LATE_FEE', 'PERCENTAGE', 5.0, 'CONDITIONAL', 5, NULL),
       (2, 'DAILY_FEE', 'FIXED', 0.75, 'CONDITIONAL', NULL, 'APPLIES_IF_LOAN_OVERDUE');
-- 0.75 daily if loan is OVERDUE

INSERT INTO LOAN_PRODUCT (id, name, min_amount, max_amount, interest_rate, tenure_type, min_tenure, max_tenure)
VALUES (3, 'Easy EMI Loan - 6 Months', 500.00, 5000.00, 12.0, 'MONTHS', 6, 6),
       (4, 'Standard EMI Loan - 12 to 24 Months', 1000.00, 10000.00, 10.5, 'MONTHS', 12, 24);

INSERT INTO FEE_CONFIGURATION (loan_product_id, fee_type, calculation_type, fee_amount, application_time,
                               days_after_due_for_late_fee, conditions)
VALUES (3, 'SERVICE_FEE', 'FIXED', 75.00, 'ORIGINATION', NULL, NULL),
       (3, 'LATE_FEE', 'FIXED', 50.00, 'CONDITIONAL', 7, NULL);

INSERT INTO FEE_CONFIGURATION (loan_product_id, fee_type, calculation_type, fee_amount, application_time,
                               days_after_due_for_late_fee, conditions)
VALUES (4, 'SERVICE_FEE', 'PERCENTAGE', 1.0, 'POST_DISBURSEMENT', NULL, NULL),
       (4, 'LATE_FEE', 'PERCENTAGE', 3.0, 'CONDITIONAL', 5, NULL);