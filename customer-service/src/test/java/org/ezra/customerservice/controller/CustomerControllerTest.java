package org.ezra.customerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ezra.customerservice.dto.CustomerRequestDto;
import org.ezra.customerservice.dto.CustomerResponseDto;
import org.ezra.customerservice.dto.UpdateLoanLimitRequestDto;
import org.ezra.customerservice.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerRequestDto customerRequestDto;
    private CustomerResponseDto customerResponseDto;
    private UpdateLoanLimitRequestDto updateLoanLimitRequestDto;

    @BeforeEach
    void setUp() {
        customerRequestDto = CustomerRequestDto.builder()
                .firstName("Jane")
                .lastName("Controller")
                .email("jane.controller@example.com")
                .phoneNumber("0987654321")
                .initialLoanLimit(BigDecimal.valueOf(2000))
                .build();

        customerResponseDto = CustomerResponseDto.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Controller")
                .email("jane.controller@example.com")
                .phoneNumber("0987654321")
                .currentLoanLimit(BigDecimal.valueOf(2000))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .loanLimitHistory(Collections.emptyList())
                .build();

        updateLoanLimitRequestDto = UpdateLoanLimitRequestDto.builder()
                .newLoanLimit(BigDecimal.valueOf(2500))
                .reason("Performance Review")
                .build();
    }

    @Test
    void createCustomer_whenValidInput_returnsCreated() throws Exception {
        given(customerService.createCustomer(any(CustomerRequestDto.class))).willReturn(customerResponseDto);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is(customerResponseDto.getEmail())));
    }

    @Test
    void createCustomer_whenInvalidEmail_returnsBadRequest() throws Exception {
        customerRequestDto.setEmail("invalid-email");

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("Email should be valid")));
    }

    @Test
    void getCustomerById_whenCustomerExists_returnsOk() throws Exception {
        Long customerId = 1L;
        given(customerService.getCustomerById(customerId)).willReturn(customerResponseDto);

        mockMvc.perform(get("/api/v1/customers/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customerId.intValue())));
    }

    @Test
    void updateCustomerLoanLimit_whenValidInput_returnsOk() throws Exception {
        Long customerId = 1L;
        customerResponseDto.setCurrentLoanLimit(updateLoanLimitRequestDto.getNewLoanLimit());
        given(customerService.updateCustomerLoanLimit(eq(customerId), any(UpdateLoanLimitRequestDto.class)))
                .willReturn(customerResponseDto);

        mockMvc.perform(put("/api/v1/customers/{customerId}/loan-limit", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateLoanLimitRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLoanLimit", is(updateLoanLimitRequestDto.getNewLoanLimit().doubleValue())));
    }

    @Test
    void checkLoanEligibility_whenEligible_returnsTrue() throws Exception {
        Long customerId = 1L;
        BigDecimal requestedAmount = BigDecimal.valueOf(1500);
        given(customerService.checkLoanEligibility(customerId, requestedAmount)).willReturn(true);

        mockMvc.perform(get("/api/v1/customers/{customerId}/check-eligibility", customerId)
                        .param("amount", requestedAmount.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void checkLoanEligibility_whenNotEligible_returnsFalse() throws Exception {
        Long customerId = 1L;
        BigDecimal requestedAmount = BigDecimal.valueOf(3000);
        given(customerService.checkLoanEligibility(customerId, requestedAmount)).willReturn(false);

        mockMvc.perform(get("/api/v1/customers/{customerId}/check-eligibility", customerId)
                        .param("amount", requestedAmount.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}