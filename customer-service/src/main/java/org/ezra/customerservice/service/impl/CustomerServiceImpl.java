package org.ezra.customerservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.ezra.customerservice.dto.CustomerRequestDto;
import org.ezra.customerservice.dto.CustomerResponseDto;
import org.ezra.customerservice.dto.UpdateLoanLimitRequestDto;
import org.ezra.customerservice.entity.Customer;
import org.ezra.customerservice.entity.LoanLimitChange;
import org.ezra.customerservice.exception.ValidationException;
import org.ezra.customerservice.mapper.CustomerMapper;
import org.ezra.customerservice.repository.CustomerRepository;
import org.ezra.customerservice.repository.LoanLimitHistoryRepository;
import org.ezra.customerservice.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final LoanLimitHistoryRepository loanLimitHistoryRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional
    public CustomerResponseDto createCustomer(CustomerRequestDto customerRequestDto) {
        if (customerRepository.existsByEmail(customerRequestDto.getEmail())) {
            throw new ValidationException("Customer with email " + customerRequestDto.getEmail() + " already exists.");
        }
        if (customerRepository.existsByPhoneNumber(customerRequestDto.getPhoneNumber())) {
            throw new ValidationException("Customer with phone number " + customerRequestDto.getPhoneNumber() + " already exists.");
        }

        Customer customer = customerMapper.toCustomerEntity(customerRequestDto);
        Customer savedCustomer = customerRepository.save(customer);
        LoanLimitChange initialHistory = LoanLimitChange.builder()
                .customer(savedCustomer)
                .previousLimit(BigDecimal.ZERO)
                .newLimit(savedCustomer.getCurrentLoanLimit())
                .changeTimestamp(savedCustomer.getCreatedAt())
                .reason("Initial loan limit assigned upon customer creation.")
                .changedBy("SYSTEM")
                .build();
        loanLimitHistoryRepository.save(initialHistory);
        savedCustomer.getLoanLimitHistory().add(initialHistory);
        return customerMapper.toCustomerResponseDto(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDto getCustomerById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("Customer not found with ID: " + customerId);
                });
        customer.getLoanLimitHistory().size();
        return customerMapper.toCustomerResponseDto(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDto getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("Customer not found with email: " + email);
                });
        customer.getLoanLimitHistory().size();
        return customerMapper.toCustomerResponseDto(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponseDto> getAllCustomers() {
        return customerRepository.findAll().stream()
                .peek(customer -> customer.getLoanLimitHistory().size())
                .map(customerMapper::toCustomerResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomerResponseDto updateCustomerLoanLimit(Long customerId, UpdateLoanLimitRequestDto updateRequest) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        BigDecimal previousLimit = customer.getCurrentLoanLimit();
        BigDecimal newLimit = updateRequest.getNewLoanLimit();

        if (previousLimit.compareTo(newLimit) == 0) {
            customer.getLoanLimitHistory().size();
            return customerMapper.toCustomerResponseDto(customer);
        }

        customer.setCurrentLoanLimit(newLimit);

        LoanLimitChange limitChangeRecord = LoanLimitChange.builder()
                .customer(customer)
                .previousLimit(previousLimit)
                .newLimit(newLimit)
                .changeTimestamp(LocalDateTime.now())
                .reason(updateRequest.getReason())
                .changedBy(updateRequest.getChangedBy() != null ? updateRequest.getChangedBy() : "SYSTEM_UPDATE")
                .build();

        customer.getLoanLimitHistory().add(limitChangeRecord);
        Customer updatedCustomer = customerRepository.save(customer);
        return customerMapper.toCustomerResponseDto(updatedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkLoanEligibility(Long customerId, BigDecimal requestedAmount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId + " for eligibility check."));
        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return customer.getCurrentLoanLimit().compareTo(requestedAmount) >= 0;
    }
}
