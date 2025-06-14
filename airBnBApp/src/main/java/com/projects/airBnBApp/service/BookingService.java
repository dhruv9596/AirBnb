package com.projects.airBnBApp.service;


import com.projects.airBnBApp.dto.BookingDto;
import com.projects.airBnBApp.dto.BookingRequest;
import com.projects.airBnBApp.dto.GuestDto;
import com.projects.airBnBApp.dto.HotelReportDto;
import com.stripe.model.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface BookingService {

    BookingDto initialiseBooking(BookingRequest bookingRequest);


    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initiateBooking(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

   String  getBookingStatus(Long bookingId);

    List<BookingDto> getAllBookingsByHotelId(Long hotelId);

    HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate);

    List<BookingDto> getMyBookings();
}
