package com.example.semilio.chat.repository;

import com.example.semilio.chat.model.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findByProductIdAndBuyerId(UUID productId, UUID buyerId);

    Page<Chat> findAllByBuyerIdOrSellerId(UUID buyerId, UUID sellerId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Chat c SET " +
            "c.buyerUnreadCount = CASE WHEN c.buyerId = :userId THEN 0 ELSE c.buyerUnreadCount END, " +
            "c.sellerUnreadCount = CASE WHEN c.sellerId = :userId THEN 0 ELSE c.sellerUnreadCount END " +
            "WHERE c.id = :chatId")
    void resetUnreadCount(@Param("chatId") Long chatId, @Param("userId") UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Chat c SET " +
            "c.sellerUnreadCount = c.sellerUnreadCount + 1, " +
            "c.buyerLastReadMessageId = :messageSeq, " +
            "c.buyerUnreadCount = 0, " +
            "c.lastModifiedDate = CURRENT_TIMESTAMP " +
            "WHERE c.id = :chatId")
    void updateStatsOnBuyerReply(@Param("chatId") Long chatId,
                                 @Param("messageSeq") Long messageSeq);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Chat c SET " +
            "c.buyerUnreadCount = c.buyerUnreadCount + 1, " +
            "c.sellerLastReadMessageId = :messageSeq, " +
            "c.lastModifiedDate = CURRENT_TIMESTAMP " +
            "WHERE c.id = :chatId")
    void updateStatsOnSellerReply(@Param("chatId") Long chatId,
                                  @Param("messageSeq") Long messageSeq);

}