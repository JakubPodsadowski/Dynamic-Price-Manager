package com.podsadowski.dynamicpricemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
}
