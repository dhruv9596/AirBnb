package com.projects.airBnBApp.strategy;

import com.projects.airBnBApp.entity.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PricingService {

    public BigDecimal calculateDynamicPricing(Inventory inventory){

        PricingStrategy pricingStrategy = new BasePricingStrategy();

        //Apply the additional startegies
        pricingStrategy = new SurgePricingStrategy(pricingStrategy);

        pricingStrategy = new OccupancyPricingStrategy(pricingStrategy);

        pricingStrategy = new UrgencyPricingStrategy(pricingStrategy);

        pricingStrategy = new HolidaysPricingStrategy(pricingStrategy);

        return pricingStrategy.calculatePrice(inventory);

    }

    //Return the sum of Price of this inventory
    public BigDecimal calculateTotalPrice(List<Inventory> inventoryList){

        BigDecimal totalPrice = inventoryList.stream()
                .map(this::calculateDynamicPricing)
                .reduce( BigDecimal.ZERO , BigDecimal::add);

        return totalPrice;
    }

}
