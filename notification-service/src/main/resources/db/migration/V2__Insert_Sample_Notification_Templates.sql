INSERT INTO NOTIFICATION_TEMPLATES (template_code, subject_template, body_template, default_channel, language_code,
                                    created_at, updated_at)
VALUES ('LOAN_APPLICATION_SUBMITTED',
        'Your Loan Application for {productName} has been Submitted!',
        'Dear {customerName},\n\nThank you for submitting your loan application (ID: {loanId}) for {productName} of amount {amount}.\nYour application is currently in status: {status}.\n\nWe will notify you once there is an update.\n\nSincerely,\nThe Lending Team',
        'EMAIL', 'en-US', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

       ('LOAN_APPLICATION_SUBMITTED_SMS',
        '', -- SMS typically has no subject, or it's part of the body
        'Your loan app ID {loanId} for {productName} amt {amount} is {status}. We will update you soon. -Lending Team',
        'SMS', 'en-US', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


INSERT INTO NOTIFICATION_TEMPLATES (template_code, subject_template, body_template, default_channel, language_code,
                                    created_at, updated_at)
VALUES ('LOAN_DISBURSED',
        'Congratulations! Your Loan (ID: {loanId}) has been Disbursed!',
        'Dear {customerName},\n\nWe are pleased to inform you that your loan (ID: {loanId}) of amount {disbursedAmount} has been successfully disbursed on {disbursementDate}.\n\nYour first payment will be due on {firstPaymentDueDate}.\n\nThank you,\nThe Lending Team',
        'EMAIL', 'en-US', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

       ('LOAN_DISBURSED_SMS',
        '',
        'Congrats! Your loan ID {loanId} of {disbursedAmount} has been disbursed on {disbursementDate}. First payment due {firstPaymentDueDate}. -Lending Team',
        'SMS', 'en-US', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


INSERT INTO NOTIFICATION_TEMPLATES (template_code, subject_template, body_template, default_channel, language_code,
                                    created_at, updated_at)
VALUES ('LOAN_OVERDUE',
        'Action Required: Your Loan (ID: {loanId}) is Overdue',
        'Dear {customerName},\n\nThis is a reminder that your loan (ID: {loanId}) is currently overdue. The outstanding amount is {outstandingAmount}.\nPlease make a payment at your earliest convenience to avoid further late fees.\n\nIf you have already made a payment, please disregard this message.\n\nRegards,\nThe Lending Team',
        'EMAIL', 'en-US', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

       ('LOAN_OVERDUE_SMS',
        '',
        'URGENT: Your loan ID {loanId} is overdue. Outstanding: {outstandingAmount}. Please pay now to avoid more fees. -Lending Team',
        'SMS', 'en-US', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


INSERT INTO NOTIFICATION_TEMPLATES (template_code, subject_template, body_template, default_channel, language_code,
                                    created_at, updated_at)
VALUES ('REPAYMENT_RECEIVED',
        'Payment Confirmation for Loan (ID: {loanId})',
        'Dear {customerName},\n\nWe have successfully received your payment of {amountPaid} for loan (ID: {loanId}).\nYour current outstanding balance is {outstandingAmount}.\nLoan status: {loanStatus}.\n\nThank you for your payment.\n\nSincerely,\nThe Lending Team',
        'EMAIL', 'en-US', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO NOTIFICATION_TEMPLATES (template_code, subject_template, body_template, default_channel, language_code,
                                    created_at, updated_at)
VALUES ('LOAN_CANCELLED',
        'Your Loan Application (ID: {loanId}) has been Cancelled',
        'Dear {customerName},\n\nThis email is to confirm that your loan application (ID: {loanId}) has been successfully cancelled as per your request or due to policy.\n\nIf you have any questions, please contact our support team.\n\nRegards,\nThe Lending Team',
        'EMAIL', 'en-US', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);