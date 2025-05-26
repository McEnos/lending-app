package org.ezra.lendingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private BigDecimal loanLimit;
}