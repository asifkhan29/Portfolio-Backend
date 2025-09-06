package com.student.portfolio.repository;


import com.student.portfolio.entity.UserPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPortfolioRepository extends JpaRepository<UserPortfolio, String> {
    
    @Query("SELECT p FROM UserPortfolio p WHERE p.userId = :userId")
    List<UserPortfolio> findByUserId(@Param("userId") String userId);
    
    @Query("SELECT p FROM UserPortfolio p WHERE p.isPublic = true")
    List<UserPortfolio> findByIsPublic(boolean isPublic);
    
    default boolean existsById(String id) {
        return findById(id).isPresent();
    }
}