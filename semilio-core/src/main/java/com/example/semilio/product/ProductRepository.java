package com.example.semilio.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends
        JpaRepository<Product,Long>,
        JpaSpecificationExecutor<Product>
{

    Page<Product> findAllBySeller_Id(String sellerId, Pageable pageable);

    @Query(
            value = """
        SELECT p FROM Product p 
        JOIN FETCH p.seller 
        WHERE p.status = 'ACTIVE' 
        ORDER BY FUNCTION('md5', CONCAT(CAST(p.id as string), :seed))
    """,
            countQuery = "SELECT count(p) FROM Product p WHERE p.status = 'ACTIVE'"
    )
    Page<Product> findFeaturedProducts(@Param("seed") String seed, Pageable pageable);

    Page<Product> findAllBySellerIdAndStatus(String sellerId, ProductStatus status, Pageable pageable);

    @Modifying
    @Query("UPDATE Product p SET p.stats.likes = p.stats.likes + 1 WHERE p.id = :id")
    void incrementLikes(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Product p SET p.stats.likes = p.stats.likes - 1 WHERE p.id = :id AND p.stats.likes > 0")
    void decrementLikes(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Product p SET p.stats.views = p.stats.views + 1 WHERE p.id = :id")
    void incrementViews(@Param("id") Long id);
}
