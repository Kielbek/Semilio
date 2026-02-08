package com.example.semilio.product;

import com.example.semilio.comon.dictionary.DictionaryService;
import com.example.semilio.image.ImageMapper;
import com.example.semilio.image.ImageResponse;
import com.example.semilio.product.model.Price;
import com.example.semilio.product.model.Product;
import com.example.semilio.product.model.ProductStats;
import com.example.semilio.product.request.ProductRequestDTO;
import com.example.semilio.product.response.*;
import com.example.semilio.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final DictionaryService dictionaryService;
    private final ImageMapper imageMapper;

    public ProductSummaryDTO toSummaryDTO(Product product) {
        if (product == null) return null;

        return ProductSummaryDTO.builder()
                .id(product.getId())
                .title(product.getTitle())
                .price(mapToPriceResponse(product.getPrice()))
                .condition(product.getCondition())
                .mainImage(imageMapper.imageTOImageResponse(product.getMainImage()))
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .build();
    }

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

    public Product toEntity(ProductRequestDTO dto) {
        if (dto == null) return null;

        return Product.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(Price.builder()
                        .amount(dto.getAmount())
                        .currencyCode("PLN")
                        .build())
                .condition(dto.getCondition())
                .color(dto.getColor())
                .brand(dto.getBrand())
                .size(dto.getSize())
                .status(Status.ACTIVE)
                .build();
    }

    public ProductCardResponse toProductCardResponse(Product product) {
        return toProductCardResponse(product, false);
    }

    public ProductCardResponse toProductCardResponse(Product product, boolean isLikedByCurrentUser) {
        if (product == null) return null;

        return ProductCardResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .slug(product.getSlug())
                .price(mapToPriceResponse(product.getPrice()))
                .mainImage(imageMapper.imageTOImageResponse(product.getMainImage()))
                .stats(mapToProductStatsResponse(product.getStats()))
                .condition(product.getCondition())
                .size(product.getSize())
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