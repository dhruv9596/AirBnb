package com.projects.airBnBApp.service;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateInventoryRequestDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal surgeFactor;
    private Boolean closed;
}
