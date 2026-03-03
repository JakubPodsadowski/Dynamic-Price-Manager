package com.podsadowski.dynamicpricemanager.config;

import com.podsadowski.dynamicpricemanager.entity.AppUser;
import com.podsadowski.dynamicpricemanager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if(userRepository.findByEmail("admin@system.com").isEmpty()) {
                AppUser admin = new AppUser();
                admin.setEmail("admin@system.com");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setRole("ADMIN");

                userRepository.save(admin);
                System.out.println("Admin created: admin@system.com : admin");
            }
        };
    }
}
