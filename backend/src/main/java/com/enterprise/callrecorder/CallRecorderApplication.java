package com.enterprise.callrecorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 🔴 CRITICAL: Main Spring Boot Application
 */
@SpringBootApplication
@EnableScheduling
public class CallRecorderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CallRecorderApplication.class, args);
    }
}
