package com.projects.airBnBApp.service;

import com.projects.airBnBApp.entity.Hotel;
import com.projects.airBnBApp.entity.HotelMinPrice;
import com.projects.airBnBApp.entity.Inventory;
import com.projects.airBnBApp.repository.HotelMinPriceRepository;
import com.projects.airBnBApp.repository.HotelRepository;
import com.projects.airBnBApp.repository.InventoryRepository;
import com.projects.airBnBApp.strategy.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PricingUpdateService {

    //Scheduler to Update the Inventory and HotelMinPrice tables every Hour.
    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PricingService pricingService;
    @Scheduled(cron = "0 0 * * * *")
    //@Scheduled(cron = "*/5 * * * * *")
    public void updatePrice(){
        int page = 0;
        int batchSize = 100;

        while(true){
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page , batchSize));
            if(hotelPage.isEmpty()){
                break;
            }
            hotelPage.getContent().forEach(this::updateHotelPrices);

            page++;
        }
    }

    private void updateHotelPrices( Hotel hotel) {

        log.info("Updating hotel prices for hotel ID : {}",hotel.getId());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusYears(1);

        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel , startDate , endDate);

        updateInventoryPrices(inventoryList);

        updateHotelMinPrice(hotel , inventoryList ,  startDate , endDate);

    }


    /*private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList ,  LocalDate startDate, LocalDate endDate) {

        // Compute minimum price per day for the hotel
        Map<LocalDate , BigDecimal> dailyMinPrices = inventoryList.stream().collect(Collectors.groupingBy(
                Inventory::getDate,
                Collectors.mapping(Inventory::getPrice , Collectors.minBy(Comparator.naturalOrder()))
        ))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,e ->e.getValue().orElse(BigDecimal.ZERO)));

        // Prepare HotelPrice entities in bulk
        List<HotelMinPrice> hotelPrices = new ArrayList<>();

        dailyMinPrices.forEach((date,price) -> {
            HotelMinPrice hotelMinPrice = hotelMinPriceRepository.findByHotelAndDate( hotel,date ).orElse( new HotelMinPrice(hotel,date));
            hotelMinPrice.setPrice(price);
            hotelMinPrice.setDate(date);
        });
        // Save all HotelPrice entities in bulk
        hotelMinPriceRepository.saveAll(hotelPrices);
    }*/
    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate) {
        // Compute minimum price per day for the hotel
        Map<LocalDate, BigDecimal> dailyMinPrices = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice, Collectors.minBy(Comparator.naturalOrder()))
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().orElse(BigDecimal.ZERO)));

        // Prepare HotelPrice entities in bulk
        List<HotelMinPrice> hotelPrices = new ArrayList<>();
        dailyMinPrices.forEach((date, price) -> {
            HotelMinPrice hotelPrice = hotelMinPriceRepository.findByHotelAndDate(hotel, date)
                    .orElse(new HotelMinPrice(hotel, date));
            hotelPrice.setPrice(price);
            hotelPrices.add(hotelPrice);
        });

        // Save all HotelPrice entities in bulk
        hotelMinPriceRepository.saveAll(hotelPrices);
    }
    private void updateInventoryPrices( List<Inventory> inventoryList){
            inventoryList.forEach(inventory -> {
                BigDecimal dynamicPrice = pricingService.calculateDynamicPricing(inventory);
                inventory.setPrice(dynamicPrice);
            });
            inventoryRepository.saveAll(inventoryList);
    }

}
