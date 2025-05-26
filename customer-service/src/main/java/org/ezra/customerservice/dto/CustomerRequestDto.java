package org.ezra.customerservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequestDto {

    @NotBlank(message = "First name cannot be blank")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Size(max = 150, message = "Email must be at most 150 characters")
    private String email;

    @NotBlank(message = "Phone number cannot be blank")
    private String phoneNumber;

    private String financialSummary;

    @NotNull(message = "Initial loan limit cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Loan limit must be zero or positive")
    private BigDecimal initialLoanLimit;
}