package org.ezra.customerservice.service;

import org.ezra.customerservice.dto.CustomerRequestDto;
import org.ezra.customerservice.dto.CustomerResponseDto;
import org.ezra.customerservice.dto.UpdateLoanLimitRequestDto;

import java.math.BigDecimal;
import java.util.List;

public interface CustomerService {

    CustomerResponseDto createCustomer(CustomerRequestDto customerRequestDto);

    CustomerResponseDto getCustomerById(Long customerId);

    CustomerResponseDto getCustomerByEmail(String email);

    List<CustomerResponseDto> getAllCustomers();

    CustomerResponseDto updateCustomerLoanLimit(Long customerId, UpdateLoanLimitRequestDto updateRequest);

    boolean checkLoanEligibility(Long customerId, BigDecimal requestedAmount);
}
