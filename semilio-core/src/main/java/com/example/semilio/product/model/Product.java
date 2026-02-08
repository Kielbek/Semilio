package com.example.semilio.product.model;

import com.example.semilio.category.Category;
import com.example.semilio.comon.SlugUtils;
import com.example.semilio.image.Image;
import com.example.semilio.product.Color;
import com.example.semilio.product.Condition;
import com.example.semilio.product.Status;
import com.example.semilio.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
public class Product {

    @Id
    @GeneratedValue(strategy = UUID)
    private String id;

    private String title;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private String size;

    @Enumerated(EnumType.STRING)
    private Condition condition;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String brand;

    @Enumerated(EnumType.STRING)
    private Color color;

    @Column(nullable = false)
    private Price price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    private String mainImageUrl;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "product_id")
    @OrderBy("sortOrder ASC")
    private List<Image> images;

    @Embedded
    private ProductStats stats;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(insertable = false)
    private String lastModifiedBy;

    @PrePersist
    public void generateSlug() {
        this.slug = SlugUtils.toSlug(this.title, this.id);
    }

    public Image getMainImage() {
        return (images != null && !images.isEmpty()) ? images.getFirst() : null;
    }
}
