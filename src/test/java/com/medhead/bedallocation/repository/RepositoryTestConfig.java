package com.medhead.bedallocation.repository;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.medhead.bedallocation.repository")
@EntityScan(basePackages = "com.medhead.bedallocation.model")
public class RepositoryTestConfig {
}
