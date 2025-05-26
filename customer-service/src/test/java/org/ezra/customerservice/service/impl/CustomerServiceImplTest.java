package org.ezra.customerservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.ezra.customerservice.dto.CustomerRequestDto;
import org.ezra.customerservice.dto.CustomerResponseDto;
import org.ezra.customerservice.dto.UpdateLoanLimitRequestDto;
import org.ezra.customerservice.entity.Customer;
import org.ezra.customerservice.entity.LoanLimitChange;
import org.ezra.customerservice.exception.ResourceNotFoundException;
import org.ezra.customerservice.exception.ValidationException;
import org.ezra.customerservice.mapper.CustomerMapper;
import org.ezra.customerservice.repository.CustomerRepository;
import org.ezra.customerservice.repository.LoanLimitHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanLimitHistoryRepository loanLimitHistoryRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    @Captor
    private ArgumentCaptor<LoanLimitChange> loanLimitChangeCaptor;

    private CustomerRequestDto customerRequestDto;
    private Customer customerEntity;
    private CustomerResponseDto customerResponseDto;

    @BeforeEach
    void setUp() {
        customerRequestDto = CustomerRequestDto.builder()
                .firstName("Test")
                .lastName("User")
                .email("test.user@example.com")
                .phoneNumber("1234567890")
                .initialLoanLimit(BigDecimal.valueOf(1000))
                .build();

        customerEntity = Customer.builder()
                .id(1L)
                .firstName("Test")
                .lastName("User")
                .email("test.user@example.com")
                .phoneNumber("1234567890")
                .currentLoanLimit(BigDecimal.valueOf(1000))
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .loanLimitHistory(new ArrayList<>())
                .build();

        customerResponseDto = CustomerResponseDto.builder()
                .id(1L)
                .email("test.user@example.com")
                .currentLoanLimit(BigDecimal.valueOf(1000))
                .loanLimitHistory(Collections.emptyList())
                .build();
    }

    @Test
    void createCustomer_success() {
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(customerMapper.toCustomerEntity(customerRequestDto)).thenReturn(customerEntity);
        when(customerRepository.save(any(Customer.class))).thenReturn(customerEntity);
        when(loanLimitHistoryRepository.save(any(LoanLimitChange.class))).thenAnswer(inv -> inv.getArgument(0));
        when(customerMapper.toCustomerResponseDto(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            return CustomerResponseDto.builder()
                    .id(c.getId())
                    .email(c.getEmail())
                    .currentLoanLimit(c.getCurrentLoanLimit())
                    .loanLimitHistory(c.getLoanLimitHistory() != null ?
                            c.getLoanLimitHistory().stream().map(h -> customerMapper.toLoanLimitChangeDto(h)).collect(java.util.stream.Collectors.toList()) :
                            Collections.emptyList())
                    .build();
        });
        lenient().when(customerMapper.toLoanLimitChangeDto(any(LoanLimitChange.class))).thenReturn(null);
        CustomerResponseDto result = customerService.createCustomer(customerRequestDto);
        assertNotNull(result);
        assertEquals(customerEntity.getEmail(), result.getEmail());
        assertEquals(customerEntity.getCurrentLoanLimit(), result.getCurrentLoanLimit());
        verify(customerRepository).save(customerCaptor.capture());
        assertEquals("Test", customerCaptor.getValue().getFirstName());

        verify(loanLimitHistoryRepository).save(loanLimitChangeCaptor.capture());
        assertEquals(BigDecimal.ZERO, loanLimitChangeCaptor.getValue().getPreviousLimit());
        assertEquals(customerRequestDto.getInitialLoanLimit(), loanLimitChangeCaptor.getValue().getNewLimit());
        assertEquals("Initial loan limit assigned upon customer creation.", loanLimitChangeCaptor.getValue().getReason());
        assertFalse(result.getLoanLimitHistory().isEmpty(), "Loan limit history should have one entry in response");
    }

    @Test
    void createCustomer_emailExists_throwsValidationException() {
        when(customerRepository.existsByEmail("test.user@example.com")).thenReturn(true);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> customerService.createCustomer(customerRequestDto));
        assertEquals("Customer with email test.user@example.com already exists.", exception.getMessage());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void getCustomerById_success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customerEntity));
        when(customerMapper.toCustomerResponseDto(customerEntity)).thenReturn(customerResponseDto);

        CustomerResponseDto result = customerService.getCustomerById(1L);

        assertNotNull(result);
        assertEquals(customerEntity.getEmail(), result.getEmail());
        assertTrue(result.getLoanLimitHistory().isEmpty());
    }

    @Test
    void getCustomerById_notFound_throwsResourceNotFoundException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(1L));
    }

    @Test
    void updateCustomerLoanLimit_success() {
        UpdateLoanLimitRequestDto updateRequest = UpdateLoanLimitRequestDto.builder()
                .newLoanLimit(BigDecimal.valueOf(1500))
                .reason("Good repayment")
                .changedBy("MANAGER")
                .build();

        Customer mutableCustomerEntity = Customer.builder()
                .id(customerEntity.getId())
                .firstName(customerEntity.getFirstName())
                .currentLoanLimit(customerEntity.getCurrentLoanLimit())
                .loanLimitHistory(new ArrayList<>(customerEntity.getLoanLimitHistory())) // Use a mutable list
                .build();


        when(customerRepository.findById(1L)).thenReturn(Optional.of(mutableCustomerEntity));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0)); // Return the modified entity
        when(customerMapper.toCustomerResponseDto(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            return CustomerResponseDto.builder()
                    .id(c.getId())
                    .currentLoanLimit(c.getCurrentLoanLimit())
                    .loanLimitHistory(c.getLoanLimitHistory().stream().map(h -> customerMapper.toLoanLimitChangeDto(h)).collect(java.util.stream.Collectors.toList()))
                    .build();
        });
        lenient().when(customerMapper.toLoanLimitChangeDto(any(LoanLimitChange.class))).thenReturn(null);


        CustomerResponseDto result = customerService.updateCustomerLoanLimit(1L, updateRequest);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1500), result.getCurrentLoanLimit());
        assertFalse(result.getLoanLimitHistory().isEmpty(), "Loan limit history should have an entry for the update.");


        verify(customerRepository).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();
        assertEquals(BigDecimal.valueOf(1500), savedCustomer.getCurrentLoanLimit());
        assertEquals(1, savedCustomer.getLoanLimitHistory().size());

        LoanLimitChange historyEntry = savedCustomer.getLoanLimitHistory().getFirst();
        assertEquals(customerEntity.getCurrentLoanLimit(), historyEntry.getPreviousLimit());
        assertEquals(BigDecimal.valueOf(1500), historyEntry.getNewLimit());
        assertEquals("Good repayment", historyEntry.getReason());
    }

    @Test
    void updateCustomerLoanLimit_sameLimit_noNewHistoryEntry() {
        UpdateLoanLimitRequestDto updateRequest = UpdateLoanLimitRequestDto.builder()
                .newLoanLimit(customerEntity.getCurrentLoanLimit()) // Same limit
                .reason("No change needed")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customerEntity));
        when(customerMapper.toCustomerResponseDto(customerEntity)).thenReturn(customerResponseDto);

        CustomerResponseDto result = customerService.updateCustomerLoanLimit(1L, updateRequest);

        assertNotNull(result);
        assertEquals(customerEntity.getCurrentLoanLimit(), result.getCurrentLoanLimit());
        assertTrue(result.getLoanLimitHistory().isEmpty());

        verify(customerRepository, never()).save(any(Customer.class));
        verify(loanLimitHistoryRepository, never()).save(any(LoanLimitChange.class));
    }


    @Test
    void checkLoanEligibility_eligible() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customerEntity));
        boolean eligible = customerService.checkLoanEligibility(1L, BigDecimal.valueOf(500));
        assertTrue(eligible);
    }

    @Test
    void checkLoanEligibility_notEligible() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customerEntity));
        boolean eligible = customerService.checkLoanEligibility(1L, BigDecimal.valueOf(1500));
        assertFalse(eligible);
    }

    @Test
    void checkLoanEligibility_customerNotFound_throwsResourceNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> customerService.checkLoanEligibility(1L, BigDecimal.valueOf(500)));
    }

    @Test
    void checkLoanEligibility_invalidRequestedAmount_returnsFalse() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customerEntity));

        assertFalse(customerService.checkLoanEligibility(1L, BigDecimal.ZERO));
        assertFalse(customerService.checkLoanEligibility(1L, BigDecimal.valueOf(-100)));
        assertFalse(customerService.checkLoanEligibility(1L, null));
    }
}