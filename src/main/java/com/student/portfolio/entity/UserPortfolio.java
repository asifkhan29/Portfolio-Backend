package com.student.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_portfolios")
public class UserPortfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private String photo;


    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @ElementCollection
    @CollectionTable(
        name = "user_portfolio_skills",
        joinColumns = @JoinColumn(name = "user_portfolio_id")
    )
    @Column(name = "skill")
    private List<String> skills;


    @Column(name = "is_public")
    private boolean isPublic;

    @Column(name = "user_id")
    private String userId;

    
}
