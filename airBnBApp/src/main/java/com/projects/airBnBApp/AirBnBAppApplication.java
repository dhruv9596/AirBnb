package com.projects.airBnBApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AirBnBAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(AirBnBAppApplication.class, args);
		System.out.println("Application Started..");
	}

}
