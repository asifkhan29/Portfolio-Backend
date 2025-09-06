package com.student.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Collection;
import java.util.Collections;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)  
    @Column(name = "id")
    private String id;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "email_verified")
    private boolean emailVerified;

    @Column(name = "roles")
    private String roles;
    
    @PrePersist
    public void prePersist() {
        if (roles == null || roles.isBlank()) {
            roles = "ROLE_USER";
        }
    }

    public Collection<String> getRoles() {
        return Collections.singletonList(roles);
    }

    
}
