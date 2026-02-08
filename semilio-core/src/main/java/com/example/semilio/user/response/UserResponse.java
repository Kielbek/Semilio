package com.example.semilio.user.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserResponse {
    private String id;
    private String firstName;
    private String lastName;
    private  String nickName;
    private String email;
    private String phoneNumber;
    private String bio;
    private LocalDate dateOfBirth;
    private String countryName;
    private boolean enabled;
    private boolean locked;
    private boolean credentialsExpired;
    private boolean emailVerified;
    private boolean phoneVerified;
    private String profilePictureUrl;
    private List<String> roles;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createdDate;
}
