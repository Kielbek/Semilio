package com.example.semilio.product.model;

import com.example.semilio.category.model.Category;
import com.example.semilio.comon.SlugUtils;
import com.example.semilio.dictionary.model.Brand;
import com.example.semilio.dictionary.model.Color;
import com.example.semilio.dictionary.model.Size;
import com.example.semilio.image.model.Image;
import com.example.semilio.product.enums.Condition;
import com.example.semilio.product.enums.Status;
import com.example.semilio.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.GenerationType.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_slug", columnList = "slug")
})
@SQLDelete(sql = "UPDATE products SET deleted = true WHERE id = ?")
public class Product {

    @Id
    @GeneratedValue(strategy = UUID)
    private UUID id;

    private String title;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    private Condition condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id", nullable = false)
    private Color color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_id", nullable = false)
    private Size size;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private Price price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "product_id")
    @OrderBy("sortOrder ASC")
    private List<Image> images;

    @Embedded
    private ProductStats stats;

    @Column(name = "deleted", nullable = false, columnDefinition = "boolean default false")
    private boolean deleted = false;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    @Column(insertable = false)
    private UUID lastModifiedBy;

    @PrePersist
    public void generateSlug() {
        this.slug = SlugUtils.toSlug(this.title, this.id);
    }

    public Image getMainImage() {
        return (images != null && !images.isEmpty()) ? images.getFirst() : null;
    }
}
