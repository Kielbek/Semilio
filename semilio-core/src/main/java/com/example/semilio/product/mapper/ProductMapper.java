package com.example.semilio.product.mapper;

import com.example.semilio.comon.dictionary.DictionaryService;
import com.example.semilio.image.mapper.ImageMapper;
import com.example.semilio.image.model.Image;
import com.example.semilio.image.response.ImageResponse;
import com.example.semilio.product.enums.Status;
import com.example.semilio.product.model.Price;
import com.example.semilio.product.model.Product;
import com.example.semilio.product.model.ProductStats;
import com.example.semilio.product.request.ProductRequest;
import com.example.semilio.product.response.*;
import com.example.semilio.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final DictionaryService dictionaryService;
    private final ImageMapper imageMapper;

    public ProductDetailDTO toDetailDTO(Product product) {
        if (product == null) return null;

        List<ImageResponse> imageResponses = product.getImages().stream()
                .map(imageMapper::imageTOImageResponse)
                .toList();

        return ProductDetailDTO.builder()
                .id(product.getId())
                .title(product.getTitle())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(mapToPriceResponse(product.getPrice()))
                .condition(product.getCondition())
                .color(product.getColor())
                .brand(product.getBrand())
                .size(product.getSize())
                .createdAt(product.getCreatedDate())
                .images(imageResponses)
                .status(product.getStatus())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .stats(mapToProductStatsResponse(product.getStats()))
                .seller(mapToSellerInfoResponse(product.getSeller()))
                .build();
    }

    public Product toEntity(ProductRequest dto) {
        if (dto == null) return null;

        return Product.builder()
                .title(dto.title())
                .description(dto.description())
                .price(Price.builder()
                        .amount(dto.amount())
                        .currencyCode("PLN")
                        .build())
                .condition(dto.condition())
                .status(Status.ACTIVE)
                .build();
    }

    public ProductCardResponse toProductCardResponse(Product product, Image mainImage) {
        return toProductCardResponse(product, mainImage, false);
    }

    public ProductCardResponse toProductCardResponse(Product product, Image mainImage, boolean isLikedByCurrentUser) {
        if (product == null) return null;

        return ProductCardResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .slug(product.getSlug())
                .price(mapToPriceResponse(product.getPrice()))
                .mainImage(imageMapper.imageTOImageResponse(mainImage))
                .stats(mapToProductStatsResponse(product.getStats()))
                .condition(product.getCondition())
                .size(product.getSize().getName())
                .status(product.getStatus())
                .isLikedByCurrentUser(isLikedByCurrentUser)
                .build();
    }


    private SellerInfoResponse mapToSellerInfoResponse(User seller) {
        if (seller == null) return null;

        String lang = Optional.ofNullable(seller.getPreferredLanguage()).orElse("pl");
        String translatedCountry = dictionaryService.getCountryNameByCode(seller.getCountry(), lang);

        return SellerInfoResponse.builder()
                .id(seller.getId())
                .nickName(seller.getNickName())
                .profilePictureUrl(seller.getProfilePictureUrl())
                .countryName(translatedCountry)
                .build();
    }

    public PriceResponse mapToPriceResponse(Price price) {
        if (price == null) return null;

        return PriceResponse.builder()
                .amount(price.getAmount())
                .currencyCode(price.getCurrencyCode())
                .build();
    }

    private ProductStatsResponse mapToProductStatsResponse(ProductStats stats) {
        if (stats == null) return null;

        return ProductStatsResponse.builder()
                .views(stats.getViews())
                .likes(stats.getLikes())
                .build();
    }
}