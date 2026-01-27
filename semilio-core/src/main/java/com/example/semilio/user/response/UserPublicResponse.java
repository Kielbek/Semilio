package com.example.semilio.user.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserPublicResponse {
    private String id;
    private String nickName;
    private String bio;
    private String countryName;
    private String profilePictureUrl;
    private LocalDateTime createdDate;
}