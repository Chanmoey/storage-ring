package com.moon.storagering;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;

@SpringBootApplication
public class StorageRingApplication {

    public static void main(String[] args) {
        SpringApplication.run(StorageRingApplication.class, args);
        System.out.println(UUID.randomUUID().toString().replace("-", ""));
    }

}
