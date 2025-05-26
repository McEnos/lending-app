package org.ezra.lendingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ezra.lendingservice.dto.FeeConfigurationDto;
import org.ezra.lendingservice.dto.LoanProductRequestDto;
import org.ezra.lendingservice.dto.LoanProductResponseDto;
import org.ezra.lendingservice.enums.FeeCalculationType;
import org.ezra.lendingservice.enums.FeeType;
import org.ezra.lendingservice.enums.TenureType;
import org.ezra.lendingservice.service.LoanProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanProductController.class)
class LoanProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanProductService loanProductService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoanProductRequestDto requestDto;
    private LoanProductResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = LoanProductRequestDto.builder()
                .name("Controller Test Product")
                .minAmount(BigDecimal.valueOf(200))
                .maxAmount(BigDecimal.valueOf(2000))
                .interestRate(BigDecimal.valueOf(7.5))
                .tenureType(TenureType.DAYS)
                .minTenure(30)
                .maxTenure(90)
                .feeConfigurations(Collections.singletonList(
                        FeeConfigurationDto.builder()
                                .feeType(FeeType.SERVICE_FEE)
                                .calculationType(FeeCalculationType.PERCENTAGE)
                                .value(BigDecimal.valueOf(1.5))
                                .build()
                ))
                .build();

        responseDto = LoanProductResponseDto.builder()
                .id(1L)
                .name("Controller Test Product")
                .minAmount(BigDecimal.valueOf(200))
                .feeConfigurations(Collections.emptyList())
                .build();
    }

    @Test
    void createLoanProduct_whenValidInput_returnsCreated() throws Exception {
        given(loanProductService.createLoanProduct(any(LoanProductRequestDto.class)))
                .willReturn(responseDto);
        ResultActions resultActions = mockMvc.perform(post("/api/v1/lending/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));
        resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(responseDto.getId().intValue())))
                .andExpect(jsonPath("$.name", is(responseDto.getName())));

        verify(loanProductService, times(1)).createLoanProduct(any(LoanProductRequestDto.class));
    }

    @Test
    void createLoanProduct_whenInvalidInputNameBlank_returnsBadRequest() throws Exception {
        requestDto.setName("");
        ResultActions resultActions = mockMvc.perform(post("/api/v1/lending/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("Product name cannot be blank")));
        verify(loanProductService, times(0)).createLoanProduct(any(LoanProductRequestDto.class));
    }


    @Test
    void getLoanProductById_whenProductExists_returnsOk() throws Exception {
        Long productId = 1L;
        given(loanProductService.getLoanProductById(productId)).willReturn(responseDto);
        mockMvc.perform(get("/api/v1/lending/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(productId.intValue())))
                .andExpect(jsonPath("$.name", is(responseDto.getName())));
    }

    @Test
    void getAllLoanProducts_returnsOkWithListOfProducts() throws Exception {
        List<LoanProductResponseDto> products = Collections.singletonList(responseDto);
        given(loanProductService.getAllLoanProducts()).willReturn(products);

        mockMvc.perform(get("/api/v1/lending/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(responseDto.getName())));
    }
}