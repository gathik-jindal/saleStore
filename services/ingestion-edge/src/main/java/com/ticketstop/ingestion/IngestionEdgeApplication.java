package com.ticketstop.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class IngestionEdgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(IngestionEdgeApplication.class, args);
    }
}
