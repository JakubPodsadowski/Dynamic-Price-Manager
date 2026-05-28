package com.podsadowski.dynamicpricemanager.config;

import com.podsadowski.dynamicpricemanager.entity.AppUser;
import com.podsadowski.dynamicpricemanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("admin@system.com").isEmpty()) {
                AppUser admin = new AppUser();
                admin.setEmail("admin@system.com");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setRole("ADMIN");

                userRepository.save(admin);
                log.info("Created default admin user (admin@system.com). Change password in production.");
            }

            if (userRepository.findByEmail("client@system.com").isEmpty()) {
                AppUser client = new AppUser();
                client.setEmail("client@system.com");
                client.setPassword(passwordEncoder.encode("client"));
                client.setRole("CLIENT");

                userRepository.save(client);
                log.info("Created default demo client user (client@system.com). Change password in production.");
            }
        };
    }
}
