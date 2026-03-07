package com.example.semilio.product.mapper;

import com.example.semilio.comon.dictionary.DictionaryService;
import com.example.semilio.image.mapper.ImageMapper;
import com.example.semilio.image.model.Image;
import com.example.semilio.image.response.ImageResponse;
import com.example.semilio.product.enums.Condition;
import com.example.semilio.product.enums.Status;
import com.example.semilio.product.model.Price;
import com.example.semilio.product.model.Product;
import com.example.semilio.product.request.ProductRequest;
import com.example.semilio.product.response.ProductDetailDTO;
import com.example.semilio.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ProductMapperTest {

    @Mock
    private DictionaryService dictionaryService;

    @Mock
    private ImageMapper imageMapper;

    @InjectMocks
    private ProductMapper productMapper;

    @Test
    @DisplayName("Should map valid Product entity to ProductDetailDTO correctly")
    void shouldMapProductToDetailDto() {
        UUID productUid = UUID.randomUUID();
        Product product = createSampleProduct(productUid);
        ImageResponse mockImageResponse = ImageResponse.builder().url("https://example.com/image.jpg").build();

        given(imageMapper.imageTOImageResponse(any(Image.class))).willReturn(mockImageResponse);
        given(dictionaryService.getCountryNameByCode(eq("PL"), eq("pl"))).willReturn("Polska");

        ProductDetailDTO result = productMapper.toDetailDTO(product);

        assertThat(result).isNotNull()
                .satisfies(dto -> {
                    assertThat(dto.id()).isEqualTo(productUid);
                    assertThat(dto.title()).isEqualTo("Testowy produkt");
                    assertThat(dto.condition()).isEqualTo(Condition.NEW_WITH_TAGS);

                    assertThat(dto.price().amount()).isEqualTo(new BigDecimal("100.50"));
                    assertThat(dto.seller().countryName()).isEqualTo("Polska");

                    assertThat(dto.images()).extracting(ImageResponse::getUrl)
                            .containsExactly("https://example.com/image.jpg");
                });

        then(imageMapper).should(times(1)).imageTOImageResponse(any(Image.class));
        then(dictionaryService).should(times(1)).getCountryNameByCode("PL", "pl");
    }

    @Test
    @DisplayName("Should return null and not interact with services when Product is null")
    void shouldReturnNullWhenProductIsNull() {
        ProductDetailDTO result = productMapper.toDetailDTO(null);

        assertThat(result).isNull();

        then(imageMapper).shouldHaveNoInteractions();
        then(dictionaryService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("Should map ProductRequest to Product entity with default ACTIVE status and PLN currency")
    void shouldMapRequestToEntity() {
        ProductRequest request = ProductRequest.builder()
                .title("Nowy tytuł")
                .description("Opis")
                .amount(new BigDecimal("299.99"))
                .condition(Condition.GOOD)
                .build();

        Product result = productMapper.toEntity(request);

        assertThat(result).isNotNull()
                .satisfies(entity -> {
                    assertThat(entity.getTitle()).isEqualTo("Nowy tytuł");
                    assertThat(entity.getDescription()).isEqualTo("Opis");
                    assertThat(entity.getCondition()).isEqualTo(Condition.GOOD);

                    assertThat(entity.getStatus()).isEqualTo(Status.ACTIVE);
                    assertThat(entity.getPrice().getAmount()).isEqualTo(new BigDecimal("299.99"));
                    assertThat(entity.getPrice().getCurrencyCode()).isEqualTo("PLN");
                });
    }

    private Product createSampleProduct(UUID uid) {
        Product product = new Product();
        product.setId(uid);
        product.setTitle("Testowy produkt");
        product.setCondition(Condition.NEW_WITH_TAGS);

        Price price = Price.builder().amount(new BigDecimal("100.50")).currencyCode("PLN").build();
        product.setPrice(price);

        User seller = new User();
        seller.setId(UUID.randomUUID());
        seller.setCountry("PL");
        product.setSeller(seller);

        Image image = new Image();
        product.setImages(List.of(image));

        return product;
    }
}