package com.example.semilio.message.projection;

import com.example.semilio.image.model.Image;

import java.util.UUID;

public interface ProductMainImageProjection {
    UUID getProductId();
    Image getImage();
}