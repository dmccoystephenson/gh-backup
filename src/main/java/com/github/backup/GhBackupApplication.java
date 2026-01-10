package com.github.backup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GhBackupApplication {

    public static void main(String[] args) {
        SpringApplication.run(GhBackupApplication.class, args);
    }
}
