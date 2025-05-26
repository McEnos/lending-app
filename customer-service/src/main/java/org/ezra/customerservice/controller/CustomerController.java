package org.ezra.customerservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ezra.customerservice.dto.CustomerRequestDto;
import org.ezra.customerservice.dto.CustomerResponseDto;
import org.ezra.customerservice.dto.UpdateLoanLimitRequestDto;
import org.ezra.customerservice.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponseDto> createCustomer(
            @Valid @RequestBody CustomerRequestDto customerRequestDto) {
        CustomerResponseDto createdCustomer = customerService.createCustomer(customerRequestDto);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponseDto> getCustomerById(@PathVariable Long customerId) {
        CustomerResponseDto customer = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<CustomerResponseDto> getCustomerByEmail(@PathVariable String email) {
        CustomerResponseDto customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(customer);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDto>> getAllCustomers() {
        List<CustomerResponseDto> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @PutMapping("/{customerId}/loan-limit")
    public ResponseEntity<CustomerResponseDto> updateCustomerLoanLimit(
            @PathVariable Long customerId,
            @Valid @RequestBody UpdateLoanLimitRequestDto updateRequest) {
        CustomerResponseDto updatedCustomer = customerService.updateCustomerLoanLimit(customerId, updateRequest);
        return ResponseEntity.ok(updatedCustomer);
    }

    /**
     * Endpoint used by lending-service to check loan eligibility.
     * It directly returns a boolean.
     */
    @GetMapping("/{customerId}/check-eligibility")
    public ResponseEntity<Boolean> checkLoanEligibility(
            @PathVariable Long customerId,
            @RequestParam("amount") BigDecimal requestedAmount) {
        boolean isEligible = customerService.checkLoanEligibility(customerId, requestedAmount);
        return ResponseEntity.ok(isEligible);
    }
}