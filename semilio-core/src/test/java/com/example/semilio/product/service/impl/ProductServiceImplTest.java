package com.example.semilio.product.service.impl;

import com.example.semilio.category.model.Category;
import com.example.semilio.category.repository.CategoryRepository;
import com.example.semilio.dictionary.model.Brand;
import com.example.semilio.dictionary.model.Color;
import com.example.semilio.dictionary.model.Size;
import com.example.semilio.dictionary.repository.BrandRepository;
import com.example.semilio.dictionary.repository.ColorRepository;
import com.example.semilio.dictionary.repository.SizeRepository;
import com.example.semilio.exception.BusinessException;
import com.example.semilio.exception.ErrorCode;
import com.example.semilio.image.model.Image;
import com.example.semilio.image.service.ImageService;
import com.example.semilio.product.enums.Status;
import com.example.semilio.product.mapper.ProductMapper;
import com.example.semilio.product.model.Product;
import com.example.semilio.product.repository.ProductRepository;
import com.example.semilio.product.request.ProductRequest;
import com.example.semilio.product.request.ProductSearchCriteriaRequest;
import com.example.semilio.product.response.ProductCardResponse;
import com.example.semilio.product.response.ProductDetailDTO;
import com.example.semilio.service.SecurityService;
import com.example.semilio.user.model.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private BrandRepository brandRepository;
    @Mock private ColorRepository colorRepository;
    @Mock private SizeRepository sizeRepository;
    @Mock private ProductMapper mapper;
    @Mock private ImageService imageService;
    @Mock private SecurityService securityService;
    @Mock private ProductCardEnricher productCardEnricher;

    @InjectMocks
    private ProductServiceImpl productService;

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should successfully create a product, assign dictionaries, and return card response")
        void shouldCreateProductSuccessfullyAndReturnCardResponse() {
            Authentication principal = mock(Authentication.class);
            List<MultipartFile> images = List.of(mock(MultipartFile.class));

            User seller = new User();
            seller.setId(UUID.randomUUID());

            Long categoryId = 1L;
            Long brandId = 2L;
            Long colorId = 3L;
            Long sizeId = 4L;

            ProductRequest request = ProductRequest.builder()
                    .categoryId(categoryId)
                    .brandId(brandId)
                    .colorId(colorId)
                    .sizeId(sizeId)
                    .build();

            Category category = new Category();
            Brand brand = new Brand();
            Color color = new Color();
            Size size = new Size();

            Product mappedProduct = new Product();

            Product savedProduct = new Product();
            savedProduct.setId(UUID.randomUUID());
            savedProduct.setSeller(seller);

            Image savedImage = new Image();
            List<Image> savedImagesList = List.of(savedImage);

            ProductCardResponse expectedResponse = ProductCardResponse.builder().id(savedProduct.getId()).build();

            given(securityService.getCurrentUser(principal)).willReturn(seller);
            given(mapper.toEntity(request)).willReturn(mappedProduct);

            given(categoryRepository.getReferenceById(categoryId)).willReturn(category);
            given(brandRepository.getReferenceById(brandId)).willReturn(brand);
            given(colorRepository.getReferenceById(colorId)).willReturn(color);
            given(sizeRepository.getReferenceById(sizeId)).willReturn(size);

            given(imageService.createImages(images, "products")).willReturn(savedImagesList);
            given(productRepository.save(mappedProduct)).willReturn(savedProduct);

            given(mapper.toProductCardResponse(eq(savedProduct), any())).willReturn(expectedResponse);

            ProductCardResponse result = productService.createProduct(request, images, principal);

            assertThat(result).isNotNull().isEqualTo(expectedResponse);

            assertThat(mappedProduct.getSeller()).isEqualTo(seller);
            assertThat(mappedProduct.getCategory()).isEqualTo(category);
            assertThat(mappedProduct.getBrand()).isEqualTo(brand);
            assertThat(mappedProduct.getColor()).isEqualTo(color);
            assertThat(mappedProduct.getSize()).isEqualTo(size);
            assertThat(mappedProduct.getImages()).isEqualTo(savedImagesList);

            assertThat(mappedProduct.getStats()).isNotNull()
                    .satisfies(stats -> {
                        assertThat(stats.getViews()).isZero();
                        assertThat(stats.getLikes()).isZero();
                    });

            then(productRepository).should(times(1)).save(mappedProduct);
            then(imageService).should(times(1)).createImages(images, "products");
        }
    }

    @Nested
    @DisplayName("Update Product Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should successfully update product fields and images when user is the owner")
        void shouldUpdateProductWhenUserIsOwner() {
            UUID productId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);
            List<MultipartFile> newFiles = List.of(mock(MultipartFile.class));

            User currentUser = new User();
            currentUser.setId(UUID.randomUUID());

            Product existingProduct = new Product();
            existingProduct.setId(productId);
            existingProduct.setSeller(currentUser);
            existingProduct.setImages(new java.util.ArrayList<>());

            ProductRequest request = ProductRequest.builder()
                    .title("Updated Title")
                    .description("Updated Description")
                    .amount(new java.math.BigDecimal("150.00"))
                    .build();

            List<Image> updatedImages = List.of(new Image());
            ProductCardResponse expectedResponse = ProductCardResponse.builder().id(productId).build();

            given(productRepository.findByIdWithoutSeller(productId)).willReturn(java.util.Optional.of(existingProduct));
            given(securityService.getCurrentUser(principal)).willReturn(currentUser);

            given(imageService.updateImages(
                    eq(existingProduct.getImages()),
                    eq(request.remainingImages()),
                    eq(newFiles),
                    eq("products")
            )).willReturn(updatedImages);

            given(productRepository.save(existingProduct)).willReturn(existingProduct);
            given(mapper.toProductCardResponse(eq(existingProduct), any())).willReturn(expectedResponse);

            ProductCardResponse result = productService.updateProduct(productId, request, newFiles, principal);

            assertThat(result).isNotNull().isEqualTo(expectedResponse);

            assertThat(existingProduct.getTitle()).isEqualTo("Updated Title");
            assertThat(existingProduct.getDescription()).isEqualTo("Updated Description");
            assertThat(existingProduct.getPrice().getAmount()).isEqualTo(new java.math.BigDecimal("150.00"));

            then(productRepository).should(times(1)).save(existingProduct);
        }

        @Test
        @DisplayName("Should throw BusinessException with ACCESS_DENIED when updating not owned product")
        void shouldThrowAccessDeniedWhenUpdatingNotOwnedProduct() {
            UUID productId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);

            User currentUser = new User();
            currentUser.setId(UUID.randomUUID());

            User differentOwner = new User();
            differentOwner.setId(UUID.randomUUID());

            Product existingProduct = new Product();
            existingProduct.setId(productId);
            existingProduct.setSeller(differentOwner);

            ProductRequest request = ProductRequest.builder().title("Hacked Title").build();

            given(productRepository.findByIdWithoutSeller(productId)).willReturn(java.util.Optional.of(existingProduct));
            given(securityService.getCurrentUser(principal)).willReturn(currentUser);

            Assertions.assertThatThrownBy(() ->
                            productService.updateProduct(productId, request, null, principal)
                    )
                    .isInstanceOf(com.example.semilio.exception.BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", com.example.semilio.exception.ErrorCode.ACCESS_DENIED);

            then(productRepository).should(org.mockito.Mockito.never()).save(any());
            then(imageService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Should throw BusinessException with PRODUCT_NOT_FOUND when updating non existent product")
        void shouldThrowNotFoundWhenUpdatingNonExistentProduct() {
            UUID productId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);
            ProductRequest request = ProductRequest.builder().build();

            given(productRepository.findByIdWithoutSeller(productId)).willReturn(java.util.Optional.empty());

            Assertions.assertThatThrownBy(() ->
                            productService.updateProduct(productId, request, null, principal)
                    )
                    .isInstanceOf(com.example.semilio.exception.BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", com.example.semilio.exception.ErrorCode.PRODUCT_NOT_FOUND);

            then(securityService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("Retrieve Single Product Tests")
    class RetrieveProductTests {

        @Test
        @DisplayName("Should return ProductDetailDTO when product is found by ID")
        void shouldReturnProductDetailDtoWhenProductExists() {
            UUID productId = UUID.randomUUID();
            Product existingProduct = new Product();
            existingProduct.setId(productId);

            ProductDetailDTO expectedDto = ProductDetailDTO.builder()
                    .id(productId)
                    .title("Found Product")
                    .build();

            given(productRepository.findByIdWithDetails(productId))
                    .willReturn(java.util.Optional.of(existingProduct));

            given(mapper.toDetailDTO(existingProduct)).willReturn(expectedDto);

            ProductDetailDTO result = productService.getProductById(productId);

            assertThat(result).isNotNull().isEqualTo(expectedDto);
            then(productRepository).should(times(1)).findByIdWithDetails(productId);
            then(mapper).should(times(1)).toDetailDTO(existingProduct);
        }

        @Test
        @DisplayName("Should throw BusinessException with PRODUCT_NOT_FOUND when getting non-existent product by ID")
        void shouldThrowNotFoundWhenGettingNonExistentProductById() {
            UUID productId = UUID.randomUUID();

            given(productRepository.findByIdWithDetails(productId))
                    .willReturn(java.util.Optional.empty());

            Assertions.assertThatThrownBy(() ->
                            productService.getProductById(productId)
                    )
                    .isInstanceOf(com.example.semilio.exception.BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", com.example.semilio.exception.ErrorCode.PRODUCT_NOT_FOUND);

            then(mapper).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Should return ProductDetailDTO when product is found by slug")
        void shouldReturnProductDetailDtoWhenProductWithSlugExists() {
            String slug = "test-product-slug";
            Product existingProduct = new Product();
            existingProduct.setSlug(slug);

            ProductDetailDTO expectedDto = ProductDetailDTO.builder()
                    .slug(slug)
                    .title("Found Product via Slug")
                    .build();

            given(productRepository.findBySlug(slug)).willReturn(java.util.Optional.of(existingProduct));
            given(mapper.toDetailDTO(existingProduct)).willReturn(expectedDto);

            ProductDetailDTO result = productService.getProductBySlug(slug);

            assertThat(result).isNotNull().isEqualTo(expectedDto);
            then(productRepository).should(times(1)).findBySlug(slug);
            then(mapper).should(times(1)).toDetailDTO(existingProduct);
        }

        @Test
        @DisplayName("Should throw BusinessException with PRODUCT_NOT_FOUND when getting non-existent product by slug")
        void shouldThrowNotFoundWhenGettingNonExistentProductBySlug() {
            String slug = "non-existent-slug";

            given(productRepository.findBySlug(slug)).willReturn(java.util.Optional.empty());

            Assertions.assertThatThrownBy(() ->
                            productService.getProductBySlug(slug)
                    )
                    .isInstanceOf(com.example.semilio.exception.BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", com.example.semilio.exception.ErrorCode.PRODUCT_NOT_FOUND);

            then(mapper).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("Delete Product Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should successfully delete product when user is the owner")
        void shouldDeleteProductWhenUserIsOwner() {
            UUID productId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);

            User seller = new User();
            seller.setId(userId);

            Product existingProduct = new Product();
            existingProduct.setId(productId);
            existingProduct.setSeller(seller);

            given(productRepository.findById(productId)).willReturn(java.util.Optional.of(existingProduct));
            given(securityService.getCurrentUserId(principal)).willReturn(userId);

            productService.deleteProduct(productId, principal);

            then(productRepository).should(times(1)).delete(existingProduct);
        }

        @Test
        @DisplayName("Should throw BusinessException with FORBIDDEN_ACTION when deleting not owned product")
        void shouldThrowForbiddenActionWhenDeletingNotOwnedProduct() {
            UUID productId = UUID.randomUUID();
            UUID currentUserId = UUID.randomUUID();
            UUID differentOwnerId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);

            User differentOwner = new User();
            differentOwner.setId(differentOwnerId);

            Product existingProduct = new Product();
            existingProduct.setId(productId);
            existingProduct.setSeller(differentOwner);

            given(productRepository.findById(productId)).willReturn(java.util.Optional.of(existingProduct));
            given(securityService.getCurrentUserId(principal)).willReturn(currentUserId);

            Assertions.assertThatThrownBy(() ->
                            productService.deleteProduct(productId, principal)
                    )
                    .isInstanceOf(com.example.semilio.exception.BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", com.example.semilio.exception.ErrorCode.FORBIDDEN_ACTION);

//            then(productRepository).should(org.mockito.Mockito.never()).delete(any());
        }

        @Test
        @DisplayName("Should throw BusinessException with PRODUCT_NOT_FOUND when deleting non existent product")
        void shouldThrowNotFoundWhenDeletingNonExistentProduct() {
            UUID productId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);

            given(productRepository.findById(productId)).willReturn(java.util.Optional.empty());

            Assertions.assertThatThrownBy(() ->
                            productService.deleteProduct(productId, principal)
                    )
                    .isInstanceOf(com.example.semilio.exception.BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", com.example.semilio.exception.ErrorCode.PRODUCT_NOT_FOUND);

            then(securityService).shouldHaveNoInteractions();
//            then(productRepository).should(org.mockito.Mockito.never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Change Visibility Tests")
    class ChangeVisibilityTests {

        @Test
        @DisplayName("Should change product status to HIDDEN when currently ACTIVE")
        void shouldChangeStatusToHiddenWhenProductIsActive() {
            UUID productId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);

            User seller = new User();
            seller.setId(userId);

            Product existingProduct = new Product();
            existingProduct.setId(productId);
            existingProduct.setSeller(seller);
            existingProduct.setStatus(com.example.semilio.product.enums.Status.ACTIVE);

            given(productRepository.findById(productId)).willReturn(java.util.Optional.of(existingProduct));
            given(securityService.getCurrentUserId(principal)).willReturn(userId);

            productService.changeVisibility(productId, principal);

            assertThat(existingProduct.getStatus()).isEqualTo(com.example.semilio.product.enums.Status.HIDDEN);
            then(productRepository).should(times(1)).save(existingProduct);
        }

        @Test
        @DisplayName("Should change product status to ACTIVE when currently HIDDEN")
        void shouldChangeStatusToActiveWhenProductIsHidden() {
            UUID productId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);

            User seller = new User();
            seller.setId(userId);

            Product existingProduct = new Product();
            existingProduct.setId(productId);
            existingProduct.setSeller(seller);
            existingProduct.setStatus(com.example.semilio.product.enums.Status.HIDDEN);

            given(productRepository.findById(productId)).willReturn(java.util.Optional.of(existingProduct));
            given(securityService.getCurrentUserId(principal)).willReturn(userId);

            productService.changeVisibility(productId, principal);

            assertThat(existingProduct.getStatus()).isEqualTo(com.example.semilio.product.enums.Status.ACTIVE);
            then(productRepository).should(times(1)).save(existingProduct);
        }

        @Test
        @DisplayName("Should throw BusinessException with FORBIDDEN_ACTION when changing visibility of not owned product")
        void shouldThrowForbiddenActionWhenChangingVisibilityOfNotOwnedProduct() {
            UUID productId = UUID.randomUUID();
            UUID currentUserId = UUID.randomUUID();
            UUID differentOwnerId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);

            User differentOwner = new User();
            differentOwner.setId(differentOwnerId);

            Product existingProduct = new Product();
            existingProduct.setId(productId);
            existingProduct.setSeller(differentOwner);

            given(productRepository.findById(productId)).willReturn(java.util.Optional.of(existingProduct));
            given(securityService.getCurrentUserId(principal)).willReturn(currentUserId);

            Assertions.assertThatThrownBy(() ->
                            productService.changeVisibility(productId, principal)
                    )
                    .isInstanceOf(com.example.semilio.exception.BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", com.example.semilio.exception.ErrorCode.FORBIDDEN_ACTION);

            then(productRepository).should(org.mockito.Mockito.never()).save(any());
        }
    }

    @Nested
    @DisplayName("Search and Paging Tests")
    class SearchAndRetrieveTests {

        @Test
        @DisplayName("Should return enriched page of products when search query is valid")
        void shouldReturnEnrichedPageOfProductsWhenSearchQueryIsValid() {
            ProductSearchCriteriaRequest criteria = mock(ProductSearchCriteriaRequest.class);
            Pageable pageable = PageRequest.of(0, 10);
            Authentication principal = mock(Authentication.class);

            Product product = new Product();
            Page<Product> productPage = new PageImpl<>(List.of(product));

            ProductCardResponse cardResponse = ProductCardResponse.builder().build();
            Page<ProductCardResponse> expectedPage = new PageImpl<>(List.of(cardResponse));

            given(criteria.query()).willReturn("laptop");
            given(productRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(productPage);
            given(productCardEnricher.enrichPage(productPage, principal, true)).willReturn(expectedPage);

            Page<ProductCardResponse> result = productService.search(criteria, pageable, principal);

            assertThat(result).isNotNull().isEqualTo(expectedPage);
            then(productRepository).should(times(1)).findAll(any(Specification.class), any(Pageable.class));
            then(productCardEnricher).should(times(1)).enrichPage(productPage, principal, true);
        }

        @Test
        @DisplayName("Should throw BusinessException when search query is too short")
        void shouldThrowExceptionWhenSearchQueryIsTooShort() {
            ProductSearchCriteriaRequest criteria = mock(ProductSearchCriteriaRequest.class);
            Pageable pageable = PageRequest.of(0, 10);
            Authentication principal = mock(Authentication.class);

            given(criteria.query()).willReturn("a");

            assertThatThrownBy(() -> productService.search(criteria, pageable, principal))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEARCH_QUERY_TOO_SHORT);

            then(productRepository).shouldHaveNoInteractions();
            then(productCardEnricher).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Should throw BusinessException when search query is null")
        void shouldThrowExceptionWhenSearchQueryIsNull() {
            ProductSearchCriteriaRequest criteria = mock(ProductSearchCriteriaRequest.class);
            Pageable pageable = PageRequest.of(0, 10);
            Authentication principal = mock(Authentication.class);

            given(criteria.query()).willReturn(null);

            assertThatThrownBy(() -> productService.search(criteria, pageable, principal))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEARCH_QUERY_TOO_SHORT);

            then(productRepository).shouldHaveNoInteractions();
            then(productCardEnricher).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Should return enriched page of featured products")
        void shouldReturnEnrichedPageOfFeaturedProducts() {
            String seed = "random-seed-123";
            Pageable pageable = PageRequest.of(0, 10);
            Authentication principal = mock(Authentication.class);

            Product product = new Product();
            Page<Product> productPage = new PageImpl<>(List.of(product));

            ProductCardResponse cardResponse = ProductCardResponse.builder().build();
            Page<ProductCardResponse> expectedPage = new PageImpl<>(List.of(cardResponse));

            given(productRepository.findFeaturedProducts(seed, pageable)).willReturn(productPage);
            given(productCardEnricher.enrichPage(productPage, principal, true)).willReturn(expectedPage);

            Page<ProductCardResponse> result = productService.getFeaturedProducts(principal, seed, pageable);

            assertThat(result).isNotNull().isEqualTo(expectedPage);
            then(productRepository).should(times(1)).findFeaturedProducts(seed, pageable);
        }

        @Test
        @DisplayName("Should return enriched page of current user products")
        void shouldReturnEnrichedPageOfCurrentUserProducts() {
            UUID currentUserId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);
            Authentication principal = mock(Authentication.class);

            Product product = new Product();
            Page<Product> productPage = new PageImpl<>(List.of(product));

            ProductCardResponse cardResponse = ProductCardResponse.builder().build();
            Page<ProductCardResponse> expectedPage = new PageImpl<>(List.of(cardResponse));

            given(securityService.getCurrentUserId(principal)).willReturn(currentUserId);
            given(productRepository.findAllBySeller_Id(currentUserId, pageable)).willReturn(productPage);
            given(productCardEnricher.enrichPage(productPage, principal, false)).willReturn(expectedPage);

            Page<ProductCardResponse> result = productService.getUserProducts(principal, pageable);

            assertThat(result).isNotNull().isEqualTo(expectedPage);
            then(productRepository).should(times(1)).findAllBySeller_Id(currentUserId, pageable);
        }

        @Test
        @DisplayName("Should return enriched page of active seller products")
        void shouldReturnEnrichedPageOfActiveSellerProducts() {
            UUID sellerId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);
            Authentication principal = mock(Authentication.class);

            Product product = new Product();
            Page<Product> productPage = new PageImpl<>(List.of(product));

            ProductCardResponse cardResponse = ProductCardResponse.builder().build();
            Page<ProductCardResponse> expectedPage = new PageImpl<>(List.of(cardResponse));

            given(productRepository.findAllBySellerIdAndStatus(sellerId, Status.ACTIVE, pageable)).willReturn(productPage);
            given(productCardEnricher.enrichPage(productPage, principal, true)).willReturn(expectedPage);

            Page<ProductCardResponse> result = productService.getSellerProducts(principal, sellerId, pageable);

            assertThat(result).isNotNull().isEqualTo(expectedPage);
            then(productRepository).should(times(1)).findAllBySellerIdAndStatus(sellerId, Status.ACTIVE, pageable);
        }
    }
}