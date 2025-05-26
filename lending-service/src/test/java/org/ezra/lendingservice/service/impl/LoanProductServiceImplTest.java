package org.ezra.lendingservice.service.impl;

import org.ezra.lendingservice.dto.FeeConfigurationDto;
import org.ezra.lendingservice.dto.LoanProductRequestDto;
import org.ezra.lendingservice.dto.LoanProductResponseDto;
import org.ezra.lendingservice.entity.FeeConfiguration;
import org.ezra.lendingservice.entity.LoanProduct;
import org.ezra.lendingservice.enums.FeeCalculationType;
import org.ezra.lendingservice.enums.FeeType;
import org.ezra.lendingservice.enums.TenureType;
import org.ezra.lendingservice.exception.ResourceNotFoundException;
import org.ezra.lendingservice.exception.ValidationException;
import org.ezra.lendingservice.mapper.LoanProductMapper;
import org.ezra.lendingservice.repository.LoanProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanProductServiceImplTest {

    @Mock
    private LoanProductRepository loanProductRepository;

    @Mock
    private LoanProductMapper loanProductMapper;

    @InjectMocks
    private LoanProductServiceImpl loanProductService;

    private LoanProductRequestDto requestDto;
    private LoanProduct productEntity;
    private LoanProductResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = LoanProductRequestDto.builder()
                .name("Test Product")
                .minAmount(BigDecimal.valueOf(100))
                .maxAmount(BigDecimal.valueOf(1000))
                .interestRate(BigDecimal.valueOf(5.0))
                .tenureType(TenureType.MONTHS)
                .minTenure(6)
                .maxTenure(12)
                .feeConfigurations(Collections.singletonList(
                        FeeConfigurationDto.builder()
                                .feeType(FeeType.SERVICE_FEE)
                                .calculationType(FeeCalculationType.FIXED)
                                .value(BigDecimal.TEN)
                                .build()
                ))
                .build();

        productEntity = LoanProduct.builder()
                .id(1L)
                .name("Test Product")
                .minAmount(BigDecimal.valueOf(100))
                .maxAmount(BigDecimal.valueOf(1000))
                .feeConfigurations(Collections.singletonList(FeeConfiguration.builder().build()))
                .build();

        responseDto = LoanProductResponseDto.builder().id(1L).name("Test Product").build();
    }

    @Test
    void createLoanProduct_success() {
        when(loanProductRepository.findByName(requestDto.getName())).thenReturn(Optional.empty());
        when(loanProductMapper.toEntity(any(LoanProductRequestDto.class))).thenReturn(productEntity);
        when(loanProductRepository.save(any(LoanProduct.class))).thenReturn(productEntity);
        when(loanProductMapper.toDto(any(LoanProduct.class))).thenReturn(responseDto);

        LoanProductResponseDto result = loanProductService.createLoanProduct(requestDto);

        assertNotNull(result);
        assertEquals(responseDto.getId(), result.getId());
        verify(loanProductRepository, times(1)).findByName(requestDto.getName());
        verify(loanProductRepository, times(1)).save(productEntity);
    }

    @Test
    void createLoanProduct_whenNameExists_throwsValidationException() {
        when(loanProductRepository.findByName(requestDto.getName())).thenReturn(Optional.of(productEntity));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> loanProductService.createLoanProduct(requestDto));
        assertEquals("Loan product with name 'Test Product' already exists.", exception.getMessage());
        verify(loanProductRepository, never()).save(any());
    }

    @Test
    void createLoanProduct_whenMinAmountGreaterThanMax_throwsValidationException() {
        requestDto.setMinAmount(BigDecimal.valueOf(1100)); // Invalid
        ValidationException exception = assertThrows(ValidationException.class,
                () -> loanProductService.createLoanProduct(requestDto));
        assertEquals("Min amount cannot be greater than max amount.", exception.getMessage());
    }


    @Test
    void getLoanProductById_success() {
        when(loanProductRepository.findById(1L)).thenReturn(Optional.of(productEntity));
        when(loanProductMapper.toDto(productEntity)).thenReturn(responseDto);

        LoanProductResponseDto result = loanProductService.getLoanProductById(1L);

        assertNotNull(result);
        assertEquals(responseDto.getName(), result.getName());
    }

    @Test
    void getLoanProductById_whenNotFound_throwsResourceNotFoundException() {
        when(loanProductRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> loanProductService.getLoanProductById(1L));
    }

    @Test
    void getAllLoanProducts_success() {
        when(loanProductRepository.findAll()).thenReturn(Collections.singletonList(productEntity));
        when(loanProductMapper.toDto(productEntity)).thenReturn(responseDto);
        List<LoanProductResponseDto> results = loanProductService.getAllLoanProducts();
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(responseDto.getName(), results.getFirst().getName());
    }
}