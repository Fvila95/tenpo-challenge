package com.tenpo.challenge.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@NoArgsConstructor
@Entity
@Table(name = "percentage_entity")
public class PercentageEntity {
    @Id
    private Long id;
    private Double percentage;
    private Instant timestamp;

    public PercentageEntity(Double percentage, Instant timestamp) {
        this.percentage = percentage;
        this.timestamp = timestamp;
    }
}