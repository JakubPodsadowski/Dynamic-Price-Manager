package com.podsadowski.dynamicpricemanager.service;

import com.podsadowski.dynamicpricemanager.dto.RegisterDto;
import com.podsadowski.dynamicpricemanager.dto.UserProfileDto;
import com.podsadowski.dynamicpricemanager.dto.UserProfileUpdateDto;
import com.podsadowski.dynamicpricemanager.entity.AppUser;
import com.podsadowski.dynamicpricemanager.mapper.DtoMapper;
import com.podsadowski.dynamicpricemanager.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DtoMapper dtoMapper;

    public void registerUser(RegisterDto dto, String role) {
        String normalizedEmail = dto.getEmail() == null ? "" : dto.getEmail().trim();
        if (normalizedEmail.isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        AppUser newUser = new AppUser(normalizedEmail, encodedPassword, role);
        try {
            userRepository.save(newUser);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("An account with this email already exists.", ex);
        }
    }

    public AppUser getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserProfileDto getUserProfileDto(String email) {
        return dtoMapper.toProfileDto(getUserByEmail(email));
    }

    public void updateUserProfile(String email, UserProfileUpdateDto dto) {
        AppUser user = getUserByEmail(email);
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        userRepository.save(user);
    }
}
