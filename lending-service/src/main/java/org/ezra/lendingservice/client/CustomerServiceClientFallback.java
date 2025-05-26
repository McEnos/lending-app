package org.ezra.lendingservice.client;


import org.ezra.lendingservice.dto.CustomerResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CustomerServiceClientFallback implements CustomerServiceClient {
    @Override
    public ResponseEntity<CustomerResponseDto> getCustomerById(Long customerId) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(null);
    }

    @Override
    public ResponseEntity<Boolean> isCustomerEligible(Long customerId, BigDecimal requestedAmount) {
        return ResponseEntity.ok(false);
    }
}
