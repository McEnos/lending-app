package org.ezra.customerservice.mapper;

import org.ezra.customerservice.dto.CustomerRequestDto;
import org.ezra.customerservice.dto.CustomerResponseDto;
import org.ezra.customerservice.dto.LoanLimitChangeDto;
import org.ezra.customerservice.entity.Customer;
import org.ezra.customerservice.entity.LoanLimitChange;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class CustomerMapper {


    public CustomerResponseDto toCustomerResponseDto(Customer customer) {
        if (customer == null) {
            return null;
        }
        CustomerResponseDto.CustomerResponseDtoBuilder builder = CustomerResponseDto.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .financialSummary(customer.getFinancialSummary())
                .currentLoanLimit(customer.getCurrentLoanLimit())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt());

        if (customer.getLoanLimitHistory() != null && !customer.getLoanLimitHistory().isEmpty()) {
            builder.loanLimitHistory(customer.getLoanLimitHistory().stream()
                    .map(this::toLoanLimitChangeDto)
                    .collect(Collectors.toList()));
        } else {
            builder.loanLimitHistory(Collections.emptyList());
        }

        return builder.build();
    }


    public LoanLimitChangeDto toLoanLimitChangeDto(LoanLimitChange loanLimitChange) {
        if (loanLimitChange == null) {
            return null;
        }
        return LoanLimitChangeDto.builder()
                .id(loanLimitChange.getId())
                .previousLimit(loanLimitChange.getPreviousLimit())
                .newLimit(loanLimitChange.getNewLimit())
                .changeTimestamp(loanLimitChange.getChangeTimestamp())
                .reason(loanLimitChange.getReason())
                .changedBy(loanLimitChange.getChangedBy())
                .build();
    }


    public Customer toCustomerEntity(CustomerRequestDto customerRequestDto) {
        if (customerRequestDto == null) {
            return null;
        }
        return Customer.builder()
                .firstName(customerRequestDto.getFirstName())
                .lastName(customerRequestDto.getLastName())
                .email(customerRequestDto.getEmail())
                .phoneNumber(customerRequestDto.getPhoneNumber())
                .financialSummary(customerRequestDto.getFinancialSummary())
                .currentLoanLimit(customerRequestDto.getInitialLoanLimit())
                .build();
    }

    public LoanLimitChange toLoanLimitChangeEntity(LoanLimitChangeDto dto, Customer customer) {
        if (dto == null) {
            return null;
        }
        return LoanLimitChange.builder()
                .customer(customer)
                .previousLimit(dto.getPreviousLimit())
                .newLimit(dto.getNewLimit())
                .changeTimestamp(dto.getChangeTimestamp() != null ? dto.getChangeTimestamp() : LocalDateTime.now())
                .reason(dto.getReason())
                .changedBy(dto.getChangedBy())
                .build();
    }
}