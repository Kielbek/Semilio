package com.example.semilio.role;

import com.example.semilio.user.User;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

import static jakarta.persistence.GenerationType.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "ROLES")
@EntityListeners(AuditingEntityListener.class)
public class Role {

    @Id
    @GeneratedValue(strategy = UUID)
    private String id;

    @Column(name = "NAME", nullable = false)
    private RoleName name;

    @ManyToMany(mappedBy = "roles")
    private List<User> users;

    @CreatedDate
    @Column(name = "CREATED_DATE", updatable = false, nullable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE", insertable = false)
    private LocalDateTime lastModifiedDate;

//    @CreatedBy
//    @Column(name = "CREATED_BY", nullable = false, updatable = false)
//    private String createdBy;
//
//    @LastModifiedBy
//    @Column(name = "LAST_MODIFIED_BY", insertable = false)
//    private String lastModifiedBy;

}