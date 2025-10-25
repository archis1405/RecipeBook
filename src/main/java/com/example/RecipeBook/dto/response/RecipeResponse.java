package com.example.RecipeBook.dto.response;

import lombok.*;

import java.util.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeResponse {
    private String id;
    private String title;
    private String summary;
    private java.util.List<String> ingredients;
    private java.util.List<String> steps;
    private java.util.List<String> labels;
    private Integer preparationTime;
    private Integer cookingTime;
    private Integer servings;
    private String status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ChefInfo chef;
    private List<String> imageUrls;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChefInfo {
        private String id;
        private String handle;
        private String firstName;
        private String lastName;
        private String profileImageUrl;
    }

}
