package com.example.RecipeBook.controller;

import com.example.RecipeBook.dto.response.ChefResponse;
import com.example.RecipeBook.dto.response.PaginatedResponse;
import com.example.RecipeBook.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chefs")
@RequiredArgsConstructor
@Tag(name = "Chefs", description = "Chef profile endpoints")
public class ChefController {

    private final FollowService followService;

    @GetMapping("/{chefId}")
    @Operation(summary = "Get chef profile")
    public ResponseEntity<ChefResponse> getChefProfile(
            @PathVariable UUID chefId,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(followService.getChefProfile(chefId, userEmail));
    }

    @GetMapping("/{chefId}/followers")
    @Operation(summary = "Get chef's followers")
    public ResponseEntity<PaginatedResponse<ChefResponse>> getFollowers(
            @PathVariable UUID chefId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int page_size) {

        if (page_size > 100) page_size = 100;
        return ResponseEntity.ok(followService.getFollowers(chefId, page, page_size));
    }

    @GetMapping("/{chefId}/following")
    @Operation(summary = "Get who chef follows")
    public ResponseEntity<PaginatedResponse<ChefResponse>> getFollowing(
            @PathVariable UUID chefId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int page_size) {

        if (page_size > 100) page_size = 100;
        return ResponseEntity.ok(followService.getFollowing(chefId, page, page_size));
    }

    @PostMapping("/{chefId}/follow")
    @Operation(summary = "Follow a chef", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Void> followChef(
            @PathVariable UUID chefId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        followService.followChef(chefId, userEmail);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{chefId}/unfollow")
    @Operation(summary = "Unfollow a chef", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Void> unfollowChef(
            @PathVariable UUID chefId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        followService.unfollowChef(chefId, userEmail);
        return ResponseEntity.noContent().build();
    }
}

