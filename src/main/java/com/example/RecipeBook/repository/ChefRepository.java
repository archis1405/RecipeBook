package com.example.RecipeBook.repository;

import com.example.RecipeBook.entity.Chef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ChefRepository  extends JpaRepository<Chef , UUID> {
    Optional<Chef> findByHandle(String handle);
    boolean existsByHandle(String handle);
    Optional<Chef> findByUserId(UUID userId);

    @Query("SELECT COUNT(r) FROM Recipe r WHERE r.Chef.id = :chefId AND r.status = 'PUBLISHED' ")
    Long countPublishedRecipesChefId(UUID chefId);
}
