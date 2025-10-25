package com.example.RecipeBook.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChefResponse {
    private String id;
    private String handle;
    private String firstName;
    private String lastName;
    private String bio;
    private String profileImageUrl;
    private Long recipesCount;
    private Long followersCount;
    private Long followingCount;
    private Boolean isFollowing;
    private LocalDateTime createdAt;
}
