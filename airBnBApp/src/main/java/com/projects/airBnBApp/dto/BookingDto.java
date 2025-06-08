package com.projects.airBnBApp.dto;
import com.projects.airBnBApp.entity.Hotel;
import com.projects.airBnBApp.entity.Room;
import com.projects.airBnBApp.entity.User;
import com.projects.airBnBApp.entity.enums.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class BookingDto {

    private Long id;
    private Integer roomsCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BookingStatus bookingStatus;
    private Set<GuestDto> guests;
    private BigDecimal amount;
}
