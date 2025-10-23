package com.example.RecipeBook.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "recipes" , indexes = {
        @Index(name = "idx_recipe_chef" , columnList = "chef_id"),
        @Index(name = "idx_recipe_status" , columnList = "status"),
        @Index(name = "idx_recipe_published" , columnList = "published_at")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chef_id", nullable = false)
    private Chef chef;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @ElementCollection
    @CollectionTable(name="recipe_ingredients", joinColumns=@JoinColumn(name="recipe_id"))
    @Column(name = "ingredient" , columnDefinition = "TEXT" , nullable = false)
    private List<String> ingredients = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name="recipe_steps", joinColumns=@JoinColumn(name="recipe_id"))
    @Column(name = "step" , columnDefinition = "TEXT" , nullable = false)
    @OrderColumn(name = "step_order")
    private List<String> steps = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name="recipe_labels", joinColumns=@JoinColumn(name="recipe_id"))
    @Column(name = "label")
    private List<String> labels = new ArrayList<>();

    private Integer preparationTime;
    private Integer cookingTime;
    private Integer servings;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecipeStatus status = RecipeStatus.DRAFT;


    private LocalDateTime publishedAt;

    @CreationTimestamp
    @Column(nullable = false , updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum RecipeStatus{
        DRAFT,
        PROCESSING,
        PUBLISHED,
        ARCHIVED
    }
}
