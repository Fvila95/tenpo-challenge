package com.tenpo.challenge.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestNumbersDTO {
    @NotNull(message = "firstNumber cannot be null")
    private Double firstNumber;

    @NotNull(message = "secondNumber cannot be null")
    private Double secondNumber;
}
