package com.example.semilio.product;

import com.example.semilio.comon.dictionary.DictionaryService;
import com.example.semilio.product.request.ProductRequestDTO;
import com.example.semilio.product.response.ProductCardResponse;
import com.example.semilio.product.response.ProductDetailDTO;
import com.example.semilio.product.response.ProductSummaryDTO;
import com.example.semilio.product.response.SellerInfoRequest;
import com.example.semilio.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {
    private final DictionaryService dictionaryService;

    public ProductSummaryDTO toSummaryDTO(Product product) {
        if (product == null) {
            return null;
        }

        ProductSummaryDTO dto = new ProductSummaryDTO();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setPrice(product.getPrice());
        dto.setCondition(product.getCondition());
        dto.setMainImageUrl(product.getMainImageUrl());

        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }

        return dto;
    }

    public ProductDetailDTO toDetailDTO(Product product) {
        if (product == null) {
            return null;
        }

        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCondition(product.getCondition());
        dto.setColor(product.getColor());
        dto.setBrand(product.getBrand());
        dto.setSize(product.getSize());
        dto.setCreatedAt(product.getCreatedDate());
        dto.setImageUrls(product.getImageUrls());
        dto.setStatus(product.getStatus());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
        }

        if (product.getStats() != null) {
            dto.setViews(product.getStats().getViews());
            dto.setLikes(product.getStats().getLikes());
        }

        dto.setSeller(mapToSellerInfoRequest(product.getSeller()));

        return dto;
    }

    private SellerInfoRequest mapToSellerInfoRequest(User seller) {
        String lang = seller.getPreferredLanguage() != null ? seller.getPreferredLanguage() : "pl";
        String translatedCountry = dictionaryService.getCountryNameByCode(seller.getCountry(), lang);

        return SellerInfoRequest.builder()
                .id(seller.getId())
                .nickName(seller.getNickName())
                .profilePictureUrl(seller.getProfilePictureUrl())
                .countryName(translatedCountry)
                .build();

    }

    public Product toEntity(ProductRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCondition(dto.getCondition());
        product.setColor(dto.getColor());
        product.setBrand(dto.getBrand());
        product.setSize(dto.getSize());
        product.setStatus(ProductStatus.ACTIVE);


        return product;
    }

    public ProductCardResponse toProductCardResponse(Product product) {
        return toProductCardResponse(product, false);
    }

    public ProductCardResponse toProductCardResponse(Product product, boolean isLikedByCurrentUser) {
        if (product == null) {
            return null;
        }

        return ProductCardResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .mainImageUrl(product.getMainImageUrl())
                .stats(product.getStats())
                .status(product.getStatus())
                .isLikedByCurrentUser(isLikedByCurrentUser)
                .build();

    }
}
