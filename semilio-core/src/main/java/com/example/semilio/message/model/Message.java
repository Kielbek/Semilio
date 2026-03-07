package com.example.semilio.message.model;

import com.example.semilio.message.enums.MessageType;
import com.example.semilio.message.model.payload.MessagePayload;
import com.github.f4b6a3.tsid.TsidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "messages", indexes = {
        @Index(name = "idx_message_chat", columnList = "chat_id")
})
@EntityListeners(AuditingEntityListener.class)
public class Message {
    @Id
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private MessagePayload payload;

    @CreatedDate
    @Column(name = "created_date", updatable = false, nullable = false)
    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = TsidCreator.getTsid().toLong();
        }
    }
}