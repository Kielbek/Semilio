package com.example.semilio.image.mapper;

import com.example.semilio.image.response.ImageResponse;
import com.example.semilio.image.model.Image;
import org.springframework.stereotype.Service;

@Service
public class ImageMapper {

    public ImageResponse imageTOImageResponse(Image image) {
        if (image == null) return null;

        return ImageResponse.builder()
                .id(image.getId())
                .url(image.getUrl())
                .width(image.getWidth())
                .height(image.getHeight())
                .sortOrder(image.getSortOrder())
                .build();
    }
}
