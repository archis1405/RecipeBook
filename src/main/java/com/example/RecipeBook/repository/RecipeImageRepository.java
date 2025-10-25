package com.example.RecipeBook.repository;

import com.example.RecipeBook.entity.RecipeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecipeImageRepository extends JpaRepository<RecipeImage , UUID> {

    List<RecipeImage> findByRecipeIdOrderByDisplayOrder(UUID recipeId);
}
