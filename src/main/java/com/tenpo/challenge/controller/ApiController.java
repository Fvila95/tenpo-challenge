package com.tenpo.challenge.controller;

import com.tenpo.challenge.model.PercentageCalculation;
import com.tenpo.challenge.service.PercentageCalculatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/calculation")
public class ApiController {
    private final PercentageCalculatorService percentageCalculatorService;
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);


    @Autowired
    public ApiController(PercentageCalculatorService percentageCalculatorService) {
        this.percentageCalculatorService = percentageCalculatorService;
    }

    @PostMapping("")
    public Mono<Double> calculateSum(@RequestParam Double firstNumber, @RequestParam Double secondNumber) {
        logger.info("Calculating sum for numbers: {} and {}", firstNumber, secondNumber);
        return percentageCalculatorService.calculatePercentage(firstNumber, secondNumber);
    }

    @GetMapping("")
    public Page<PercentageCalculation> getAllCalculations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Fetching calculations, page: {}, size: {}", page, size);
        return percentageCalculatorService.findAllCalculations(page, size);
    }
}
