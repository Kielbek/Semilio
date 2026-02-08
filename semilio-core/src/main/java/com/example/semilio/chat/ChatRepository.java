package com.example.semilio.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, String> {

    @Query("""
    SELECT c FROM Chat c 
    WHERE c.sender.id = :userId OR c.recipient.id = :userId 
    ORDER BY c.lastMessageDate DESC NULLS LAST
""")
    Page<Chat> findAllChatsOrderByLastMessage(@Param("userId") String userId, Pageable pageable);

    Optional<Chat> findBySenderIdAndProductId(String senderId, String productId);

    @Query("""
        SELECT c FROM Chat c 
        LEFT JOIN FETCH c.sender 
        LEFT JOIN FETCH c.recipient 
        LEFT JOIN FETCH c.product 
        WHERE c.id = :id
    """)
    Optional<Chat> findByIdWithDetails(@Param("id") String id);
}