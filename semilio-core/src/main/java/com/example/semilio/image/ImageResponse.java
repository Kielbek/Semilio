package com.example.semilio.image;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageResponse {
    String id;
    String url;
    Integer sortOrder;
    Integer width;
    Integer height;
}
