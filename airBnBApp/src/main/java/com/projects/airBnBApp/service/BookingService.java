package com.projects.airBnBApp.service;


import com.projects.airBnBApp.dto.BookingDto;
import com.projects.airBnBApp.dto.BookingRequest;
import com.projects.airBnBApp.dto.GuestDto;

import java.util.List;

public interface BookingService {

    BookingDto initialiseBooking(BookingRequest bookingRequest);


    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);
}
