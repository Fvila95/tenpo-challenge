package com.tenpo.challenge.controller;

import com.tenpo.challenge.service.PercentageCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
public class ApiController {
    private final PercentageCalculatorService percentageCalculatorService;

    @Autowired
    public ApiController(PercentageCalculatorService percentageCalculatorService) {
        this.percentageCalculatorService = percentageCalculatorService;
    }

    @GetMapping("/api-rest")
    public Mono<Double> calculateSum(@RequestParam Double firstNumber, @RequestParam Double secondNumber) {
        double sum = firstNumber + secondNumber;
        return percentageCalculatorService.calculatePercentage(firstNumber, secondNumber).map(percentage -> sum + sum * (percentage / 100));
    }
}
