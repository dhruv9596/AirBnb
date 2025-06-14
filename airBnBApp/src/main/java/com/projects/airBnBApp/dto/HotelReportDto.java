package com.projects.airBnBApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelReportDto {

    private Long bookingsCount;
    private BigDecimal totalRevenue;
    private BigDecimal avgRevenue;

}
