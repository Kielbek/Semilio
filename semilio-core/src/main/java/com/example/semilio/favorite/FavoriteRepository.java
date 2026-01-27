package com.example.semilio.favorite;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, String> {

    boolean existsByUserIdAndProductId(String userId, Long productId);

    void deleteByUserIdAndProductId(String userId, Long productId);

    @Query("""
        SELECT f FROM Favorite f 
        JOIN FETCH f.product 
        WHERE f.user.id = :userId 
        ORDER BY f.createdAt DESC
    """)
    Page<Favorite> findAllByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT f.product.id FROM Favorite f WHERE f.user.id = :userId AND f.product.id IN :productIds")
    Set<Long> findLikedProductIds(
            @Param("userId") String userId,
            @Param("productIds") List<Long> productIds
    );
}
