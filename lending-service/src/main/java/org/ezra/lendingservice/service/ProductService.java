package org.ezra.lendingservice.service;

import org.ezra.lendingservice.dto.ProductDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductDto create(ProductDto productDto);
    List<ProductDto> getProducts(Pageable pageable);
}
