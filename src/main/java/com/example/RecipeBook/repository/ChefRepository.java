package com.example.RecipeBook.repository;

import com.example.RecipeBook.entity.Chef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChefRepository  extends JpaRepository<Chef , UUID> {
    Optional<Chef> findByHandle(String handle);
    boolean existsByHandle(String handle);
    Optional<Chef> findByUserId(UUID userId);

    @Query("SELECT COUNT(r) FROM Recipe r WHERE r.chef.id = :chefId AND r.status = 'PUBLISHED' ")
    Long countPublishedRecipesChefId(UUID chefId);
}
