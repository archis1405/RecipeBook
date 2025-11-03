package com.example.RecipeBook.controller;

import com.example.RecipeBook.dto.request.RecipeRequest;
import com.example.RecipeBook.dto.response.PaginatedResponse;
import com.example.RecipeBook.dto.response.RecipeResponse;
import com.example.RecipeBook.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
@Tag(name = "Recipes" , description = "Endpoints for managing recipes")
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping
    @Operation(summary = "List recipes from followed chefs" , security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<PaginatedResponse<RecipeResponse>> getRecipes(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String published_from,
            @RequestParam(required = false) String published_to,
            @RequestParam(required = false) String chef_id,
            @RequestParam(required = false) String chef_handle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int page_size,
            Authentication authentication){

        if(page_size>100){
            page_size = 100;
        }

        String userEmail = authentication.getName();
        PaginatedResponse<RecipeResponse> response = recipeService.getRecipesFromFollowedChefs(
                userEmail,q,published_from,published_to,chef_id,chef_handle,page,page_size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recipe by ID")
    public ResponseEntity<RecipeResponse> getRecipe(@PathVariable UUID id) {
        return ResponseEntity.ok(recipeService.getRecipeById(id));
    }

    @PostMapping
    @Operation(summary = "Create new recipe", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<RecipeResponse> createRecipe(@Valid @RequestBody RecipeRequest request , Authentication authentication){
        String userEmail = authentication.getName();
        RecipeResponse response = recipeService.createRecipe(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update recipe", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<RecipeResponse> updateRecipe(
            @PathVariable UUID id,
            @Valid @RequestBody RecipeRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(recipeService.updateRecipe(id, request, userEmail));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete recipe", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Void> deleteRecipe(@PathVariable UUID id, Authentication authentication) {
        String userEmail = authentication.getName();
        recipeService.deleteRecipe(id, userEmail);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish recipe", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<RecipeResponse> publishRecipe(
            @PathVariable UUID id,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(recipeService.publishRecipe(id, userEmail));
    }

    @PostMapping("/{id}/images")
    @Operation(summary = "Upload recipe images", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<RecipeResponse> uploadImages(
            @PathVariable UUID id,
            @RequestParam("images") List<MultipartFile> files,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(recipeService.uploadImages(id, files, userEmail));
    }
}
