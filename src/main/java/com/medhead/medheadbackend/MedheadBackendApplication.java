package com.medhead.medheadbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan(basePackages = {"com.medhead"})
public class MedheadBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedheadBackendApplication.class, args);
    }

}
