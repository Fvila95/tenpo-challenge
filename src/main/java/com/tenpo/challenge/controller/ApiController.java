package com.tenpo.challenge.controller;

import com.tenpo.challenge.dto.PercentageCalculationDTO;
import com.tenpo.challenge.dto.RequestNumbersDTO;
import com.tenpo.challenge.exception.PercentageCalculationException;
import com.tenpo.challenge.service.PercentageCalculatorService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/calculator")
public class ApiController {
    private final PercentageCalculatorService percentageCalculatorService;
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    public ApiController(PercentageCalculatorService percentageCalculatorService) {
        this.percentageCalculatorService = percentageCalculatorService;
    }

    @PostMapping("/sum-with-percentage")
    public Mono<ResponseEntity<String>> calculateSum(@Valid @RequestBody RequestNumbersDTO requestNumbersDTO) {
        logger.info("Calculating sum for numbers: {}", requestNumbersDTO);
        return percentageCalculatorService.calculatePercentage(requestNumbersDTO)
                .map(result -> ResponseEntity.ok(result.toString()))
                .onErrorResume(PercentageCalculationException.class, e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unable to obtain any percentage to perform the calculation.")))
                .doOnError(throwable -> logger.error(throwable.getMessage()));
    }

    @GetMapping("/history")
    public Mono<Page<PercentageCalculationDTO>> getAllCalculations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Fetching calculations, page: {}, size: {}", page, size);
        return percentageCalculatorService.findAllCalculations(page, size)
                .doOnError(throwable -> logger.error(throwable.getMessage()));
    }
}
