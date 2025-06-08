package com.projects.airBnBApp.strategy;
import com.projects.airBnBApp.entity.Inventory;

import java.math.BigDecimal;
public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);

}
