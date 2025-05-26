package org.ezra.lendingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ezra.lendingservice.dto.LoanApplicationRequestDto;
import org.ezra.lendingservice.dto.LoanResponseDto;
import org.ezra.lendingservice.dto.RepaymentRequestDto;
import org.ezra.lendingservice.dto.RepaymentResponseDto;
import org.ezra.lendingservice.enums.LoanStatus;
import org.ezra.lendingservice.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
class LoanControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanService loanService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoanApplicationRequestDto applicationRequestDto;
    private LoanResponseDto loanResponseDto;
    private RepaymentRequestDto repaymentRequestDto;
    private RepaymentResponseDto repaymentResponseDto;

    @BeforeEach
    void setUp() {
        applicationRequestDto = LoanApplicationRequestDto.builder()
                .customerId(1L)
                .productId(1L)
                .amount(BigDecimal.valueOf(1000))
                .tenure(12)
                .isInstallmentLoan(true)
                .build();

        loanResponseDto = LoanResponseDto.builder()
                .id(1L)
                .customerId(1L)
                .principalAmount(BigDecimal.valueOf(1000))
                .status(LoanStatus.PENDING_APPROVAL)
                .originationDate(LocalDate.now())
                .build();

        repaymentRequestDto = RepaymentRequestDto.builder()
                .amount(BigDecimal.valueOf(100))
                .paymentMethod("CARD")
                .build();

        repaymentResponseDto = RepaymentResponseDto.builder()
                .id(1L)
                .loanId(1L)
                .amount(BigDecimal.valueOf(100))
                .message("Repayment successful")
                .build();
    }

    @Test
    void applyForLoan_whenValidInput_returnsCreated() throws Exception {
        given(loanService.applyForLoan(any(LoanApplicationRequestDto.class))).willReturn(loanResponseDto);

        mockMvc.perform(post("/api/v1/lending/loans/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applicationRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(loanResponseDto.getId().intValue())))
                .andExpect(jsonPath("$.status", is(LoanStatus.PENDING_APPROVAL.toString())));
    }

    @Test
    void applyForLoan_whenInvalidCustomerId_returnsBadRequest() throws Exception {
        applicationRequestDto.setCustomerId(null); // Invalid

        mockMvc.perform(post("/api/v1/lending/loans/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applicationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("Customer ID cannot be null")));
    }


    @Test
    void getLoanById_whenLoanExists_returnsOk() throws Exception {
        Long loanId = 1L;
        given(loanService.getLoanById(loanId)).willReturn(loanResponseDto);

        mockMvc.perform(get("/api/v1/lending/loans/{loanId}", loanId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(loanId.intValue())));
    }

    @Test
    void getLoansByCustomerId_returnsOkWithListOfLoans() throws Exception {
        Long customerId = 1L;
        List<LoanResponseDto> loans = Collections.singletonList(loanResponseDto);
        given(loanService.getLoansByCustomerId(customerId)).willReturn(loans);

        mockMvc.perform(get("/api/v1/lending/loans/customer/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(loanResponseDto.getId().intValue())));
    }

    @Test
    void disburseLoan_returnsOk() throws Exception {
        Long loanId = 1L;
        loanResponseDto.setStatus(LoanStatus.OPEN); // Expected after disbursement
        given(loanService.disburseLoan(loanId)).willReturn(loanResponseDto);

        mockMvc.perform(post("/api/v1/lending/loans/{loanId}/disburse", loanId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(LoanStatus.OPEN.toString())));
    }

    @Test
    void cancelLoan_returnsOk() throws Exception {
        Long loanId = 1L;
        loanResponseDto.setStatus(LoanStatus.CANCELLED); // Expected after cancellation
        given(loanService.cancelLoan(loanId)).willReturn(loanResponseDto);

        mockMvc.perform(post("/api/v1/lending/loans/{loanId}/cancel", loanId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(LoanStatus.CANCELLED.toString())));
    }

    @Test
    void processRepayment_whenValidInput_returnsOk() throws Exception {
        Long loanId = 1L;
        given(loanService.processRepayment(eq(loanId), any(RepaymentRequestDto.class)))
                .willReturn(repaymentResponseDto);

        mockMvc.perform(post("/api/v1/lending/loans/{loanId}/repayments", loanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(repaymentRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Repayment successful")));
    }

    @Test
    void processRepayment_whenInvalidAmount_returnsBadRequest() throws Exception {
        Long loanId = 1L;
        repaymentRequestDto.setAmount(BigDecimal.ZERO); // Invalid

        mockMvc.perform(post("/api/v1/lending/loans/{loanId}/repayments", loanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(repaymentRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("Repayment amount must be positive")));
    }

    @Test
    void triggerOverdueProcessing_returnsOk() throws Exception {
        doNothing().when(loanService).processOverdueLoans();

        mockMvc.perform(post("/api/v1/lending/loans/admin/process-overdue")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Overdue loan processing job triggered manually."));

        verify(loanService, times(1)).processOverdueLoans();
    }
}