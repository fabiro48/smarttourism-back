package com.smarttourism.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartTourismApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartTourismApplication.class, args);
    }
}
