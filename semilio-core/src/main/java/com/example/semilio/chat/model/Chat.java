package com.example.semilio.chat.model;

import com.github.f4b6a3.tsid.TsidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chats", indexes = {
        @Index(name = "idx_chat_item_buyer", columnList = "item_id, buyer_id", unique = true),
        @Index(name = "idx_chat_buyer", columnList = "buyer_id"),
        @Index(name = "idx_chat_seller", columnList = "seller_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Chat {

    @Id
    private Long id;

    @Column(name = "item_id", nullable = false)
    private UUID productId;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "buyer_unread_count", nullable = false)
    private Integer buyerUnreadCount = 0;

    @Column(name = "buyer_last_read_message_id", nullable = false)
    private Long buyerLastReadMessageId = 0L;

    @Column(name = "seller_unread_count", nullable = false)
    private Integer sellerUnreadCount = 0;

    @Column(name = "seller_last_read_message_id", nullable = false)
    private Long sellerLastReadMessageId = 0L;

    @CreatedDate
    @Column(name = "created_date", updatable = false, nullable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", insertable = false)
    private LocalDateTime lastModifiedDate;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = TsidCreator.getTsid().toLong();
        }
    }


}