package com.podsadowski.dynamicpricemanager.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@Profile("demo-seed")
public class DemoDataSeedRunner {

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public CommandLineRunner runDemoSeed(DemoSeedService demoSeedService) {
        return args -> demoSeedService.seedIfNeeded();
    }
}
