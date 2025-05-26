package org.ezra.lendingservice.client;

import org.ezra.lendingservice.dto.CustomerResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "customer-service", path = "/api/v1/customers", fallback = CustomerServiceClientFallback.class)
public interface CustomerServiceClient {

    @GetMapping("/{id}")
    ResponseEntity<CustomerResponseDto> getCustomerById(@PathVariable("id") Long customerId);

    @GetMapping("/{customerId}/check-eligibility")
    ResponseEntity<Boolean> isCustomerEligible(@PathVariable("customerId") Long customerId,
                                               @RequestParam("amount") BigDecimal requestedAmount);
}