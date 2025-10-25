package com.example.RecipeBook.repository;

import com.example.RecipeBook.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe , UUID> {
    @Query("SELECT r FROM Recipe r WHERE r.chef.id IN :chefIds " +
            "AND r.status = 'PUBLISHED' " +
            "AND (:keyword IS NULL OR " +
            "LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.summary) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:publishedFrom IS NULL OR r.publishedAt >= :publishedFrom) " +
            "AND (:publishedTo IS NULL OR r.publishedAt <= :publishedTo) " +
            "ORDER BY r.publishedAt DESC")
    Page<Recipe> findRecipesFromFollowedChefs(
            @Param("chefIds")List<UUID> chefIds,
            @Param("keyword")String keyword,
            @Param("publishedFrom")LocalDateTime publishedFrom,
            @Param("publishedTo")LocalDateTime publishedTo,
            Pageable pageable
            );

    Page<Recipe> findByChefIdAndStatus(UUID chefId , Recipe.RecipeStatus status , Pageable pageable);

    List<Recipe> findByChefId(UUID chefId);
}
