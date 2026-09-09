package com.example.chatcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChatCoreServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatCoreServiceApplication.class, args);
    }
}