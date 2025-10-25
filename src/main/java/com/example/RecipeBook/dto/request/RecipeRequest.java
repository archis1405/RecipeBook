package com.example.RecipeBook.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255 , message = "Title must be at most 255 characters")
    private String title;

    @Size(max = 500 , message = "Title must be at most 500 characters")
    private String summary;

    @NotEmpty(message = "At least one ingredient is required")
    private List<String> ingredients;

    @NotEmpty(message = "At least one step is required")
    private List<String> steps;

    private List<String> labels;

    @Min(value = 0 , message = "Preparation time must be positive value")
    private Integer preparationTime;

    @Min(value = 0 , message = "Cooking time must be positive value")
    private Integer CookingTime;

    @Min(value = 1 , message = "Servings must be atleast 1")
    private Integer servings;
}
