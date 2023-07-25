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
    private final PercentageCalculatorService externalService;

    @Autowired
    public ApiController(PercentageCalculatorService externalService) {
        this.externalService = externalService;
    }

    @GetMapping("/api-rest")
    public Mono<Double> calculateSum(@RequestParam double num1, @RequestParam double num2) {
        double sum = num1 + num2;
        return externalService.calculatePercentage().map(percentage -> sum + sum * (percentage / 100));
    }
}
