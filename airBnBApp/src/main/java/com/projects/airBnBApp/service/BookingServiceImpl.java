package com.projects.airBnBApp.service;

import com.projects.airBnBApp.dto.BookingDto;
import com.projects.airBnBApp.dto.BookingRequest;
import com.projects.airBnBApp.dto.GuestDto;
import com.projects.airBnBApp.entity.*;
import com.projects.airBnBApp.entity.enums.BookingStatus;
import com.projects.airBnBApp.exception.ResourceNotFoundException;
import com.projects.airBnBApp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final BookingRepository bookingRepository;
    private final ModelMapper modelMapper;
    private final GuestRepository guestRepository;

    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequest bookingRequest) {

        log.info("Initialising booking for Hotel : {}, room : {}, date {} - {} " + bookingRequest.getHotelId() , bookingRequest.getRoomId() , bookingRequest.getRoomId() , bookingRequest.getCheckInDate() , bookingRequest.getCheckOutDate());

        Hotel hotel = hotelRepository
                .findById(bookingRequest.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel Not Found with id : " + bookingRequest.getHotelId() )
        );

        Room room = roomRepository
                .findById(bookingRequest.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room Not Found with id : " + bookingRequest.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository.findAndLockInventory(room.getId() , bookingRequest.getCheckInDate() , bookingRequest.getCheckOutDate() , bookingRequest.getRoomsCount());

        Long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate() , bookingRequest.getCheckOutDate()) +1 ;

        if( inventoryList.size() != daysCount ){
            throw new IllegalStateException("Room is not available anymore");
        }

        //Reserve the room/ update the booked count of inventories

        for ( Inventory inventory : inventoryList ){
            inventory.setReservedCount(inventory.getReservedCount() + bookingRequest.getRoomsCount());
        }
        inventoryRepository.saveAll(inventoryList);

        //Create the booking
        //TODO : calculate dynamic amount

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(BigDecimal.TEN)
                .build();

        log.info("Booking Entity is ready");

        booking = bookingRepository.save(booking);
        return modelMapper.map( booking , BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding guests for booking for id : {} " + bookingId);

        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking Not Found with id : " + bookingId )
                );

        if( hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has already Expired");
        }

        if(booking.getBookingStatus() != BookingStatus.RESERVED){
            throw new IllegalStateException("Booking is not under reserved state, cannot add guests");
        }

        for( GuestDto guestDto : guestDtoList ){
            Guest guest = modelMapper.map(guestDto , Guest.class);
            guest.setUser(getCurrentUser());
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);

        booking = bookingRepository.save(booking);

        return modelMapper.map(booking , BookingDto.class);

    }

    public boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser(){
        User user = new User();
        user.setId(1L);
        return user;
    }

}
