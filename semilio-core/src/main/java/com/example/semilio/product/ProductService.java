package com.example.semilio.product;

import com.example.semilio.product.request.ProductRequestDTO;
import com.example.semilio.product.request.ProductSearchCriteriaRequest;
import com.example.semilio.product.response.ProductCardResponse;
import com.example.semilio.product.response.ProductDetailDTO;
import com.example.semilio.product.response.ProductSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface ProductService {

    ProductCardResponse createProduct(ProductRequestDTO productDTO, List<MultipartFile> images, Authentication principal);

    ProductSummaryDTO updateProduct(Long productId, ProductRequestDTO productDTO);

    ProductDetailDTO getProductById(Long productId);

    void deleteProduct(Long productId, Authentication principal);

    Page<ProductCardResponse> getUserProducts(Authentication principal, Pageable pageable);

    void changeVisibility(Long productId, Authentication principal);

    Page<ProductCardResponse> getFeaturedProducts(Authentication principal, String effectiveSeed, Pageable pageable);

    Page<ProductCardResponse> search(ProductSearchCriteriaRequest criteria, Pageable pageable, Authentication auth);

    Page<ProductCardResponse> getSellerProducts(Authentication principal, String sellerId, Pageable pageable);

    void addViewAsync(Long productId);
}
