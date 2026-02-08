package com.example.semilio.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByChatId(String chatId, Pageable pageable);

//    @Modifying
//    @Query("UPDATE Message m SET m.state = 'READ' " +
//            "WHERE m.chatId = :chatId " +
//            "AND m.receiverId = :userId " +
//            "AND m.state = 'SENT'")
//    void markChatAsRead(@Param("chatId") String chatId, @Param("userId") String userId);
}