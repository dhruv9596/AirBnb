package com.projects.airBnBApp.service;

import com.projects.airBnBApp.dto.BookingDto;
import com.projects.airBnBApp.dto.BookingRequest;
import com.projects.airBnBApp.dto.GuestDto;
import com.projects.airBnBApp.entity.*;
import com.projects.airBnBApp.entity.enums.BookingStatus;
import com.projects.airBnBApp.exception.ResourceNotFoundException;
import com.projects.airBnBApp.exception.UnAuthorizedException;
import com.projects.airBnBApp.repository.*;
import com.projects.airBnBApp.strategy.PricingService;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

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
    private final CheckoutService checkoutService;

    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontendurl;

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

        inventoryRepository.initBooking(room.getId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());

        //Create the booking
        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);

        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(totalPrice)
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

        User user = getCurrentUser();

        if( user.equals(booking.getUser())){
            throw new UnAuthorizedException("Booking doesn't belong to this user with id : {} " + user.getId());
        }

        if( hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has already Expired");
        }

        if(booking.getBookingStatus() != BookingStatus.RESERVED){
            throw new IllegalStateException("Booking is not under reserved state, cannot add guests");
        }

        for( GuestDto guestDto : guestDtoList ){
            Guest guest = modelMapper.map(guestDto , Guest.class);
            guest.setUser(user);
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);

        booking = bookingRepository.save(booking);

        return modelMapper.map(booking , BookingDto.class);

    }

    @Override
    @Transactional
    public String initiateBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking Not Found with if : {}"+ bookingId));

        User user = getCurrentUser();

        if( user.equals(booking.getUser())){
            throw new UnAuthorizedException("Booking doesn't belong to this user with id : {} " + user.getId());
        }

        if( hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has already Expired");
        }

        String sessionUrl = checkoutService.getCheckoutSession(booking , frontendurl+"/payments/success" , frontendurl+"/payments/failure");

        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);

        bookingRepository.save(booking);

        return sessionUrl;

    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        if("checkout.session.completed".equals(event.getType())){
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

            if(session == null) return;
                String sessionId = session.getId();
                Booking booking = bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(()->
                        new ResourceNotFoundException("Booking  not found with Session Id : {}"+ sessionId));
                booking.setBookingStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);

                inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId() , booking.getCheckInDate() , booking.getCheckOutDate() , booking.getRoomsCount());

                inventoryRepository.confirmBooking(booking.getRoom().getId() , booking.getCheckInDate() , booking.getCheckOutDate() , booking.getRoomsCount());

                log.info("Booking Confirmed with Booking Id : {} for  Session Id : {}"+ booking.getId()+sessionId);

        }else{
            log.warn("Unhandles Event Type : {}", event.getType());
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking Not Found with id : {}"+ bookingId));

        User user = getCurrentUser();

        if( user.equals(booking.getUser())){
            throw new UnAuthorizedException("Booking doesn't belong to this user with id : {} " + user.getId());
        }

        if( booking.getBookingStatus() != BookingStatus.CONFIRMED){
            throw new IllegalStateException("Only Confirmed Booking can be cancelled.");
        }


        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId() , booking.getCheckInDate() , booking.getCheckOutDate() , booking.getRoomsCount());

        inventoryRepository.cancelBooking(booking.getRoom().getId() , booking.getCheckInDate() , booking.getCheckOutDate() , booking.getRoomsCount());

        //handle the Refund
        try{
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())

                    .build();

            Refund.create(refundParams);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }



    }

    @Override
    public String getBookingStatus(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking Not Found with if : {}"+ bookingId));

        User user = getCurrentUser();

        if( user.equals(booking.getUser())){
            throw new UnAuthorizedException("Booking doesn't belong to this user with id : {} " + user.getId());
        }

        return booking.getBookingStatus().name();
    }

    public boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser(){

        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
