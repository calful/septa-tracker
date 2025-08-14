package com.calful.septatracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SeptaTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeptaTrackerApplication.class, args);
	}

}
