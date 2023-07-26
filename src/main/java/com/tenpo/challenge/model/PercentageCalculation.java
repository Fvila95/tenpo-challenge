package com.tenpo.challenge.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@NoArgsConstructor
@Entity
@Table(name = "percentage_calculation")
public class PercentageCalculation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Double percentage;
    private Double firstNumber;
    private Double secondNumber;
    private Double result;
    private Instant timestamp;

    public PercentageCalculation(Double percentage, Double firstNumber, Double secondNumber, Double result, Instant timestamp) {
        this.percentage = percentage;
        this.firstNumber = firstNumber;
        this.secondNumber = secondNumber;
        this.result = result;
        this.timestamp = timestamp;
    }
}