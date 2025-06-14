package com.projects.airBnBApp.service;

import com.projects.airBnBApp.dto.HotelDto;
import com.projects.airBnBApp.dto.HotelInfoDto;
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

import static com.projects.airBnBApp.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService{

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Creating a new hotel with name : {}", hotelDto.getName());
        Hotel hotel = modelMapper.map( hotelDto , Hotel.class);
        hotel.setActive(false);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);
        hotel = hotelRepository.save(hotel);
        log.info("Created hotel with ID : {}", hotelDto.getId());
        return modelMapper.map(hotel , HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Getting hotel with ID : {}", id);
        Hotel hotel = hotelRepository
                        .findById(id)
                        .orElseThrow( () -> new ResourceNotFoundException("Hotel Not Found with id : " + id ));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if( !user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user doesn't own this Hotel with id : {} "+id);
        }

        return modelMapper.map( hotel , HotelDto.class);

    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating hotel with ID : {}", id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("Hotel Not Found with id : " + id ));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if( !user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user doesn't own this Hotel with id : {} "+id);
        }

        modelMapper.map( hotelDto , hotel );
        hotel.setId(id);
        hotel = hotelRepository.save(hotel);
        return modelMapper.map(hotel , HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long id) {

        log.info("Deleting hotel with ID : {}", id);
        Hotel hotel = hotelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hotel Not Found with id : " + id ));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if( !user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user doesn't own this Hotel with id : {} "+id);
        }

        for(Room room : hotel.getRooms()){
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }
        hotelRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void activateHotelById(Long id) {
        log.info("Activating hotel with ID : {}", id);
        Hotel hotel = hotelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hotel Not Found with id : " + id ));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if( !user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user doesn't own this Hotel with id : {} "+id);
        }

        hotel.setActive(true);
        log.info("Activate field " , hotel.getActive() ," for hotel with ID : {}", id );

        //Assuming only do it once
        for(Room room : hotel.getRooms()){
            inventoryService.initializeRoomForAYear(room);
        }

        hotelRepository.save(hotel);
    }

    //public
    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel Not Found with id : " + hotelId )
        );

        List<RoomDto> rooms = hotel.getRooms()
                .stream()
                .map( (element) -> modelMapper.map(element , RoomDto.class)).toList();

        return new HotelInfoDto( modelMapper.map(hotel , HotelDto.class) , rooms);

    }

    @Override
    public List<HotelDto> getAllHotels() {

        User user = getCurrentUser();
        log.info("Getting All Hotels for admin user with ID : {} : "+ user.getId());
        List<Hotel> hotels = hotelRepository.findByOwner(user);
        return hotels.stream()
                .map((element) -> modelMapper.map(element, HotelDto.class))
                .collect(Collectors.toList());

    }
}
