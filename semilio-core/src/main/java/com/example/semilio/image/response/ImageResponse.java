package com.example.semilio.image.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageResponse {
    UUID id;
    String url;
    Integer sortOrder;
    Integer width;
    Integer height;
}
