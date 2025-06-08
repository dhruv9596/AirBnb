package com.projects.airBnBApp.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@Data
public class HotelInfoDto {
    private HotelDto hotel;
    private List<RoomDto> rooms;
}
