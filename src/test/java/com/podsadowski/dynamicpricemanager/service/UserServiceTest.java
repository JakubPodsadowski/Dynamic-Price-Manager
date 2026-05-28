package com.podsadowski.dynamicpricemanager.service;

import com.podsadowski.dynamicpricemanager.dto.RegisterDto;
import com.podsadowski.dynamicpricemanager.dto.UserProfileUpdateDto;
import com.podsadowski.dynamicpricemanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Test
    void registerUser_andUpdateProfile() {
        RegisterDto reg = new RegisterDto();
        reg.setEmail("userservice@test.local");
        reg.setPassword("pass1234");
        userService.registerUser(reg, "CLIENT");

        UserProfileUpdateDto update = new UserProfileUpdateDto();
        update.setFirstName("Ewa");
        update.setLastName("Test");
        update.setPhoneNumber("111222333");
        userService.updateUserProfile("userservice@test.local", update);

        var profile = userService.getUserProfileDto("userservice@test.local");
        assertThat(profile.getFirstName()).isEqualTo("Ewa");
        assertThat(userRepository.findByEmail("userservice@test.local")).isPresent();
    }

    @Test
    void registerUser_rejectsDuplicateEmail() {
        RegisterDto reg = new RegisterDto();
        reg.setEmail("dup@test.local");
        reg.setPassword("pass1234");
        userService.registerUser(reg, "CLIENT");

        assertThatThrownBy(() -> userService.registerUser(reg, "CLIENT"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
