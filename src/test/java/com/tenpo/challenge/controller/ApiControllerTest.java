package com.tenpo.challenge.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tenpo.challenge.exception.PercentageCalculationException;
import com.tenpo.challenge.model.PercentageCalculation;
import com.tenpo.challenge.service.PercentageCalculatorService;
import com.tenpo.challenge.utils.PageableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ApiControllerTest {

    @Mock
    private PercentageCalculatorService percentageCalculatorService;

    @InjectMocks
    private ApiController apiController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void calculateSum_Success() {
        Double firstNumber = 5.0;
        Double secondNumber = 5.0;
        Double expectedValue = 11.0;

        when(percentageCalculatorService.calculatePercentage(any(Double.class), any(Double.class))).thenReturn(Mono.just(expectedValue));

        StepVerifier.create(apiController.calculateSum(firstNumber, secondNumber))
                .expectNext(ResponseEntity.ok(expectedValue.toString()))
                .verifyComplete();
    }

    @Test
    void calculateSum_PercentageCalculationException() {
        Double firstNumber = 2.0;
        Double secondNumber = 3.0;

        when(percentageCalculatorService.calculatePercentage(any(Double.class), any(Double.class))).thenReturn(Mono.error(new PercentageCalculationException("Percentage calculation error")));

        StepVerifier.create(apiController.calculateSum(firstNumber, secondNumber))
                .expectNext(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unable to obtain any percentage to perform the calculation."))
                .verifyComplete();
    }

    @Test
    void getAllCalculations() {
        String expectedJson = "{\"content\":[{\"id\":1,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:10:36.443076Z\"},{\"id\":2,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:19:24.294533Z\"},{\"id\":52,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:28:26.154274Z\"},{\"id\":53,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:28:27.867419Z\"},{\"id\":54,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:28:28.699331Z\"},{\"id\":55,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:28:29.201931Z\"},{\"id\":56,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:28:31.143289Z\"},{\"id\":57,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:28:31.738722Z\"},{\"id\":102,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:40:27.415280Z\"},{\"id\":103,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:40:28.161514Z\"}],\"pageable\":{\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"offset\":0,\"pageNumber\":0,\"pageSize\":10,\"paged\":true,\"unpaged\":false},\"last\":false,\"totalPages\":3,\"totalElements\":26,\"first\":true,\"size\":10,\"number\":0,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"numberOfElements\":10,\"empty\":false}";
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());
        PageableResponse<PercentageCalculation> page = null;
        try {
            page = mapper.readValue(expectedJson, mapper.getTypeFactory().constructParametricType(PageableResponse.class, PercentageCalculation.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        when(percentageCalculatorService.findAllCalculations(any(Integer.class), any(Integer.class))).thenReturn(Mono.just(page));

        StepVerifier.create(apiController.getAllCalculations(0, 10))
                .expectNext(page)
                .verifyComplete();
    }
}