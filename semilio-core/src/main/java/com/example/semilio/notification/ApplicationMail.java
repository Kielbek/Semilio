package com.example.semilio.notification;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Data
@Builder
@Entity
@Table(name = "mails")
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationMail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String toMail;

    @Column(length = 100, nullable = false)
    private String fromMail;

    @Column(length = 100, nullable = false)
    private String subject;

    @Lob
    @Column(nullable = false)
    private String message;

    @Temporal(TemporalType.TIMESTAMP)
    private Date sendAt;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    private Boolean isSent = false;

    @Lob
    private String actionUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 150)
    private EmailTemplates template;
}