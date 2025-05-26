package org.ezra.lendingservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ezra.lendingservice.dto.LoanApplicationRequestDto;
import org.ezra.lendingservice.dto.LoanResponseDto;
import org.ezra.lendingservice.dto.RepaymentRequestDto;
import org.ezra.lendingservice.dto.RepaymentResponseDto;
import org.ezra.lendingservice.service.LoanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lending/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/apply")
    public ResponseEntity<LoanResponseDto> applyForLoan(
            @Valid @RequestBody LoanApplicationRequestDto applicationRequestDto) {
        LoanResponseDto loanResponse = loanService.applyForLoan(applicationRequestDto);
        return new ResponseEntity<>(loanResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponseDto> getLoanById(@PathVariable Long loanId) {
        LoanResponseDto loanResponse = loanService.getLoanById(loanId);
        return ResponseEntity.ok(loanResponse);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<LoanResponseDto>> getLoansByCustomerId(@PathVariable Long customerId) {
        List<LoanResponseDto> loans = loanService.getLoansByCustomerId(customerId);
        return ResponseEntity.ok(loans);
    }

    @PostMapping("/{loanId}/disburse")
    public ResponseEntity<LoanResponseDto> disburseLoan(@PathVariable Long loanId) {
        LoanResponseDto loanResponse = loanService.disburseLoan(loanId);
        return ResponseEntity.ok(loanResponse);
    }

    @PostMapping("/{loanId}/cancel")
    public ResponseEntity<LoanResponseDto> cancelLoan(@PathVariable Long loanId) {
        LoanResponseDto loanResponse = loanService.cancelLoan(loanId);
        return ResponseEntity.ok(loanResponse);
    }

    @PostMapping("/{loanId}/repayments")
    public ResponseEntity<RepaymentResponseDto> processRepayment(
            @PathVariable Long loanId,
            @Valid @RequestBody RepaymentRequestDto repaymentRequestDto) {
        RepaymentResponseDto repaymentResponse = loanService.processRepayment(loanId, repaymentRequestDto);
        return ResponseEntity.ok(repaymentResponse);
    }
}
