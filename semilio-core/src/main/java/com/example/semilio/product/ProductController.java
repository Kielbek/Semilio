package com.example.semilio.product;

import com.example.semilio.product.request.ProductSearchCriteriaRequest;
import com.example.semilio.product.response.ProductCardResponse;
import com.example.semilio.product.response.ProductDetailDTO;
import com.example.semilio.product.request.ProductRequestDTO;
import com.example.semilio.product.response.ProductSummaryDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductCardResponse> create(
            @RequestPart("dto") @Valid ProductRequestDTO dto,
            @RequestPart("images") List<MultipartFile> images,
            final Authentication principal
    ) {

        ProductCardResponse createdProduct = productService.createProduct(dto, images, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductSummaryDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO dto) {

        ProductSummaryDTO updatedProduct = productService.updateProduct(id, dto);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ProductDetailDTO> getById(@PathVariable Long id) {
        ProductDetailDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication principal
            ){
        productService.deleteProduct(id, principal);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<Void> toggleVisibility(
            @PathVariable Long id,
            Authentication principal
            ) {
        productService.changeVisibility(id, principal);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user")
    public ResponseEntity<Page<ProductCardResponse>> getUserProducts(
            Authentication principal,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ProductCardResponse> userProducts = productService.getUserProducts(principal, pageable);
        return ResponseEntity.ok(userProducts);
    }

    @GetMapping("/public/featured")
    public ResponseEntity<Page<ProductCardResponse>> getFeaturedProducts(
            @RequestParam(required = false) String seed,
            @PageableDefault(size = 8) Pageable pageable,
            Authentication principal
    ) {
        String effectiveSeed = (seed != null && !seed.isEmpty()) ? seed : UUID.randomUUID().toString();

        return ResponseEntity.ok(productService.getFeaturedProducts(principal, effectiveSeed, pageable));
    }

    @GetMapping("/public/search")
    public ResponseEntity<Page<ProductCardResponse>> searchProducts(
            ProductSearchCriteriaRequest criteria,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication principal
    ) {
        Page<ProductCardResponse> results = productService.search(criteria, pageable, principal);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/public/seller/{sellerId}")
    public ResponseEntity<Page<ProductCardResponse>> getSellerProducts(
            Authentication principal,
            @PathVariable String sellerId,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ProductCardResponse> sellerProducts = productService.getSellerProducts(principal, sellerId, pageable);
        return ResponseEntity.ok(sellerProducts);
    }

    @PatchMapping("/public/{id}/view")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void incrementView(@PathVariable Long id) {
        productService.addViewAsync(id);
    }
}