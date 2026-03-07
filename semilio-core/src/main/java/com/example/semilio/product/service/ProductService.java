package com.example.semilio.product.service;

import com.example.semilio.product.request.ProductRequest;
import com.example.semilio.product.request.ProductSearchCriteriaRequest;
import com.example.semilio.product.response.ProductCardResponse;
import com.example.semilio.product.response.ProductDetailDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public interface ProductService {

    ProductCardResponse createProduct(ProductRequest productDTO,
                                      List<MultipartFile> images,
                                      Authentication principal);

    ProductCardResponse updateProduct(UUID productId,
                                      ProductRequest productDTO,
                                      List<MultipartFile> newFiles,
                                      Authentication principal);

    ProductDetailDTO getProductById(UUID productId);

    void deleteProduct(UUID productId, Authentication principal);

    Page<ProductCardResponse> getUserProducts(Authentication principal,
                                              Pageable pageable);

    void changeVisibility(UUID productId, Authentication principal);

    Page<ProductCardResponse> getFeaturedProducts(Authentication principal,
                                                  String effectiveSeed,
                                                  Pageable pageable);

    Page<ProductCardResponse> search(ProductSearchCriteriaRequest criteria,
                                     Pageable pageable,
                                     Authentication auth);

    Page<ProductCardResponse> getSellerProducts(Authentication principal,
                                                UUID sellerId,
                                                Pageable pageable);

    void addViewAsync(UUID productId);

    ProductDetailDTO getProductBySlug(String slug);
}
