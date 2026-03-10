package com.example.semilio.product.repository;

import com.example.semilio.message.projection.ProductMainImageProjection;
import com.example.semilio.product.enums.Status;
import com.example.semilio.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends
        JpaRepository<Product, UUID>,
        JpaSpecificationExecutor<Product>
{
    @EntityGraph(attributePaths = {"size"})
    Page<Product> findAllBySellerIdAndStatusNot(UUID sellerId, Status status, Pageable pageable);

    @EntityGraph(attributePaths = {"size"})
    @Query(value = """
        SELECT p FROM Product p 
        WHERE p.status = 'ACTIVE' 
        ORDER BY function('md5', concat(cast(p.id as string), :seed))
    """, countQuery = "SELECT count(p) FROM Product p WHERE p.status = 'ACTIVE'")
    Page<Product> findFeaturedProducts(@Param("seed") String seed, Pageable pageable);

    Page<Product> findAllBySellerIdAndStatus(UUID sellerId, Status status, Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.seller WHERE p.id = :id")
    Optional<Product> findByIdWithUser(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"category", "images", "size", "brand", "color"})
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithoutSeller(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"seller", "category", "images", "size", "brand", "color"})
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithDetails(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"seller", "category", "images", "size", "brand", "color"})
    Optional<Product> findBySlug(String slug);

    @Override
    @EntityGraph(attributePaths = {"size"})
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    @Query("SELECT p.id as productId, i as image " +
            "FROM Product p JOIN p.images i " +
            "WHERE p.id IN :productIds AND i.sortOrder = 0")
    List<ProductMainImageProjection> findMainImagesForProducts(@Param("productIds") List<UUID> productIds);

    @Modifying
    @Query("UPDATE Product p SET p.stats.likes = p.stats.likes + 1 WHERE p.id = :id")
    void incrementLikes(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Product p SET p.stats.likes = p.stats.likes - 1 WHERE p.id = :id AND p.stats.likes > 0")
    void decrementLikes(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Product p SET p.stats.views = p.stats.views + 1 WHERE p.id = :id")
    void incrementViews(@Param("id") UUID id);
}