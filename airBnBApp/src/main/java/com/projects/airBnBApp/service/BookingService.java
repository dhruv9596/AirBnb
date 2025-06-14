package com.projects.airBnBApp.service;


import com.projects.airBnBApp.dto.BookingDto;
import com.projects.airBnBApp.dto.BookingRequest;
import com.projects.airBnBApp.dto.GuestDto;
import com.stripe.model.Event;

import java.util.List;
import java.util.Map;

public interface BookingService {

    BookingDto initialiseBooking(BookingRequest bookingRequest);


    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initiateBooking(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

   String  getBookingStatus(Long bookingId);
}
