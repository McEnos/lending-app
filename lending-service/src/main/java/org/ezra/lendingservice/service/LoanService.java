package org.ezra.lendingservice.service;

import org.ezra.lendingservice.dto.*;

import java.util.List;

public interface LoanService {
    LoanResponseDto applyForLoan(LoanApplicationRequestDto applicationDto);
    LoanResponseDto getLoanById(Long loanId);
    List<LoanResponseDto> getLoansByCustomerId(Long customerId);
    LoanResponseDto disburseLoan(Long loanId);
    LoanResponseDto cancelLoan(Long loanId);
    RepaymentResponseDto processRepayment(Long loanId, RepaymentRequestDto repaymentDto);
    void processOverdueLoans();
}
