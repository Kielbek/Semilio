package com.example.semilio.user.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileUpdateRequest {
    private String nickName;
    private String bio;
    private String countryCode;
}
