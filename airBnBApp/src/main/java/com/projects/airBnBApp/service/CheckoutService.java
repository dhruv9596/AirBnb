package com.projects.airBnBApp.service;

import com.projects.airBnBApp.entity.Booking;

public interface CheckoutService {

    String getCheckoutSession(Booking booking , String successUrl , String failureUrl);


}
