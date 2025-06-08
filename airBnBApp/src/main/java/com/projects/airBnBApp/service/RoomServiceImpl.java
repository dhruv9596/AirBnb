package com.projects.airBnBApp.service;

import com.projects.airBnBApp.dto.RoomDto;
import com.projects.airBnBApp.entity.Hotel;
import com.projects.airBnBApp.entity.Room;
import com.projects.airBnBApp.entity.User;
import com.projects.airBnBApp.exception.ResourceNotFoundException;
import com.projects.airBnBApp.exception.UnAuthorizedException;
import com.projects.airBnBApp.repository.HotelRepository;
import com.projects.airBnBApp.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    @Override
    public RoomDto createNewRoom(Long hotelId , RoomDto roomDto) {
        log.info("Creating a New Room in Hotel with ID : {}  " + hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow( () -> new ResourceNotFoundException("Hotel Not Found with id : " + hotelId ));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if( !user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user doesn't own this Hotel with id : {} "+hotelId);
        }

        Room room = modelMapper.map( roomDto, Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);

        if(hotel.getActive()){
            inventoryService.initializeRoomForAYear(room);
        }

        return modelMapper.map( room , RoomDto.class );
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all Room in Hotel with ID : {}  " + hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow( () -> new ResourceNotFoundException("Hotel Not Found with id : " + hotelId ));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if( !user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user doesn't own this Hotel with id : {} "+hotelId);
        }

        return hotel.getRooms()
                .stream()
                .map((element) -> modelMapper.map( element , RoomDto.class) )
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting the Room with ID : {}  " + roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow( () -> new ResourceNotFoundException("Room Not Found with id : " + roomId ));

        return modelMapper.map( room , RoomDto.class );
    }

    @Override
    @Transactional
    public void deleteRoomById(Long roomId) {
        log.info("Deleting the Room with ID : {}  " + roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow( () -> new ResourceNotFoundException("Room Not Found with id : " + roomId ));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if( !user.equals(room.getHotel().getOwner())){
            throw new UnAuthorizedException("This user doesn't own this Hotel with room id : {} "+ room.getId());
        }

        inventoryService.deleteAllInventories(room);
        roomRepository.deleteById(roomId);
    }
}
