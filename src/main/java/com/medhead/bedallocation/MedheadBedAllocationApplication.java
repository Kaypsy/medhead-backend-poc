package com.medhead.bedallocation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {"com.medhead"})
@EnableJpaAuditing
@EntityScan(basePackages = {"com.medhead"})
public class MedheadBedAllocationApplication {
    public static void main(String[] args) {
        SpringApplication.run(MedheadBedAllocationApplication.class, args);
    }
}
