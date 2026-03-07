package com.example.semilio.favorite.repository;

import com.example.semilio.favorite.model.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    void deleteByUserIdAndProductId(UUID userId, UUID productId);

    @Query("""
        SELECT f FROM Favorite f 
        WHERE f.userId = :userId 
        ORDER BY f.createdAt DESC
    """)
    Page<Favorite> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT f.productId FROM Favorite f WHERE f.userId = :userId AND f.productId IN :productIds")
    Set<UUID> findLikedProductIds(
            @Param("userId") UUID userId,
            @Param("productIds") List<UUID> productIds
    );
}
