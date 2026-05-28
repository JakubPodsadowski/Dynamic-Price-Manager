package com.podsadowski.dynamicpricemanager.service;

import com.podsadowski.dynamicpricemanager.dto.RegisterDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CustomUserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private UserService userService;

    @Test
    void loadUserByUsername() {
        RegisterDto reg = new RegisterDto();
        reg.setEmail("details@test.local");
        reg.setPassword("secret123");
        userService.registerUser(reg, "CLIENT");

        var details = customUserDetailsService.loadUserByUsername("details@test.local");
        assertThat(details.getUsername()).isEqualTo("details@test.local");
        assertThat(details.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));
    }

    @Test
    void loadUserByUsername_notFound() {
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("missing@test.local"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
