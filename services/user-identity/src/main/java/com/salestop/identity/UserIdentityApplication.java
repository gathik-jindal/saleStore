package com.salestop.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class UserIdentityApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserIdentityApplication.class, args);
    }
}
