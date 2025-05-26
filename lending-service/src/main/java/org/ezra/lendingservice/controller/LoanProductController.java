package org.ezra.lendingservice.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ezra.lendingservice.dto.LoanProductRequestDto;
import org.ezra.lendingservice.dto.LoanProductResponseDto;
import org.ezra.lendingservice.service.LoanProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lending/products")
@RequiredArgsConstructor
public class LoanProductController {
    private final LoanProductService loanProductService;

    @PostMapping
    public ResponseEntity<LoanProductResponseDto> createLoanProduct(
            @Valid @RequestBody LoanProductRequestDto productRequestDto) {
        LoanProductResponseDto createdProduct = loanProductService.createLoanProduct(productRequestDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanProductResponseDto> getLoanProductById(@PathVariable Long id) {
        LoanProductResponseDto product = loanProductService.getLoanProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<List<LoanProductResponseDto>> getAllLoanProducts() {
        List<LoanProductResponseDto> products = loanProductService.getAllLoanProducts();
        return ResponseEntity.ok(products);
    }
}
