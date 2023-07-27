package com.tenpo.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PercentageCalculationDTO {
    private Double percentage;
    private Double firstNumber;
    private Double secondNumber;
    private Double result;
    private Instant timestamp;
}
