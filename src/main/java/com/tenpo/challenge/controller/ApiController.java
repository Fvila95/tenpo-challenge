package com.tenpo.challenge.controller;

import com.tenpo.challenge.service.ExternalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
public class ApiController {
    private final ExternalService externalService;

    @Autowired
    public ApiController(ExternalService externalService) {
        this.externalService = externalService;
    }

    @GetMapping("/api-rest")
    public Mono<Double> calculateSum(@RequestParam double num1, @RequestParam double num2) {
        double sum = num1 + num2;
        double percentage = externalService.getPercentage();
        double result = sum + sum * (percentage / 100);

        // Devuelve el resultado envuelto en un Mono porque estamos utilizando WebFlux
        return Mono.just(result);
    }
}
