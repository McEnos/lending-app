package org.ezra.lendingservice.service;

import org.ezra.lendingservice.dto.LoanProductRequestDto;
import org.ezra.lendingservice.dto.LoanProductResponseDto;

import java.util.List;

public interface LoanProductService {
    LoanProductResponseDto createLoanProduct(LoanProductRequestDto productDto);
    LoanProductResponseDto getLoanProductById(Long id);
    List<LoanProductResponseDto> getAllLoanProducts();
}
