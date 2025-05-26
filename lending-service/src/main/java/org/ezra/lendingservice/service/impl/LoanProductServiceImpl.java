package org.ezra.lendingservice.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ezra.lendingservice.dto.LoanProductRequestDto;
import org.ezra.lendingservice.dto.LoanProductResponseDto;
import org.ezra.lendingservice.entity.LoanProduct;
import org.ezra.lendingservice.exception.ResourceNotFoundException;
import org.ezra.lendingservice.exception.ValidationException;
import org.ezra.lendingservice.mapper.LoanProductMapper;
import org.ezra.lendingservice.repository.LoanProductRepository;
import org.ezra.lendingservice.service.LoanProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class LoanProductServiceImpl implements LoanProductService {

    private final LoanProductRepository loanProductRepository;
    private final LoanProductMapper loanProductMapper;

    @Override
    @Transactional
    public LoanProductResponseDto createLoanProduct(LoanProductRequestDto productDto) {
        if (loanProductRepository.findByName(productDto.getName()).isPresent()) {
            throw new ValidationException("Loan product with name '" + productDto.getName() + "' already exists.");
        }
        if (productDto.getMinAmount().compareTo(productDto.getMaxAmount()) > 0) {
            throw new ValidationException("Min amount cannot be greater than max amount.");
        }
        if (productDto.getMinTenure() > productDto.getMaxTenure()) {
            throw new ValidationException("Min tenure cannot be greater than max tenure.");
        }
        LoanProduct loanProduct = loanProductMapper.toEntity(productDto);
        Long currentId = loanProductRepository.findMaxId();
        loanProduct.setId(currentId + 1);
        LoanProduct savedProduct = loanProductRepository.save(loanProduct);
        return loanProductMapper.toDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanProductResponseDto getLoanProductById(Long id) {
        LoanProduct loanProduct = loanProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanProduct not found with ID: " + id));
        return loanProductMapper.toDto(loanProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanProductResponseDto> getAllLoanProducts() {
        log.debug("Fetching all loan products");
        return loanProductRepository.findAll().stream()
                .map(loanProductMapper::toDto)
                .collect(Collectors.toList());
    }
}