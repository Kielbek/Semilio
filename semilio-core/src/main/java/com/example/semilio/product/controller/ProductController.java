package com.example.semilio.product.controller;

import com.example.semilio.product.request.ProductSearchCriteriaRequest;
import com.example.semilio.product.response.ProductCardResponse;
import com.example.semilio.product.response.ProductDetailDTO;
import com.example.semilio.product.request.ProductRequest;
import com.example.semilio.product.service.ProductService;
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
@RequestMapping("/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductCardResponse> create(
            @RequestPart("dto") @Valid ProductRequest dto,
            @RequestPart("images") List<MultipartFile> images,
            Authentication principal
    ) {

        ProductCardResponse createdProduct = productService.createProduct(dto, images, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductCardResponse> update(
            @PathVariable UUID id,
            @Valid @RequestPart("dto") ProductRequest dto,
            @RequestPart(value = "newFiles", required = false) List<MultipartFile> newFiles,
            Authentication principal
    ) {

        ProductCardResponse updatedProduct = productService.updateProduct(id, dto, newFiles, principal);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ProductDetailDTO> getById(@PathVariable UUID id) {
        ProductDetailDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/public/items/{slug}")
    public ResponseEntity<ProductDetailDTO> getBySlug(@PathVariable String slug) {
        ProductDetailDTO product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Authentication principal
            ){
        productService.deleteProduct(id, principal);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<Void> toggleVisibility(
            @PathVariable UUID id,
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
            @PathVariable UUID sellerId,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ProductCardResponse> sellerProducts = productService.getSellerProducts(principal, sellerId, pageable);
        return ResponseEntity.ok(sellerProducts);
    }

    @PatchMapping("/public/{id}/view")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void incrementView(@PathVariable UUID id) {
        productService.addViewAsync(id);
    }
}