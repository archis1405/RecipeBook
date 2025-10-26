package com.example.RecipeBook.service;

import com.example.RecipeBook.dto.response.ChefResponse;
import com.example.RecipeBook.dto.response.PaginatedResponse;
import com.example.RecipeBook.entity.Chef;
import com.example.RecipeBook.entity.ChefFollow;
import com.example.RecipeBook.entity.User;
import com.example.RecipeBook.exception.ResourceNotFoundException;
import com.example.RecipeBook.repository.ChefFollowRepository;
import com.example.RecipeBook.repository.ChefRepository;
import com.example.RecipeBook.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final ChefFollowRepository followRepository;
    private final ChefRepository chefRepository;
    private final UserRepository userRepository;

    @Transactional
    public void followChef(UUID chefIdToFollow, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found Exception"));

        Chef follower = user.getChef();

        if (follower == null) {
            throw new IllegalStateException("User is not a chef");
        }

        Chef following = chefRepository.findById(chefIdToFollow)
                .orElseThrow(() -> new ResourceNotFoundException("Chef Not Found"));

        if (follower.getId().equals(following.getId())) {
            throw new IllegalStateException("Cannot follow yourself");
        }

        if (followRepository.existsByFollowerIdAndFollowingId(follower.getId(), following.getId())) {
            throw new IllegalStateException("Already following this chef");
        }

        ChefFollow chefFollow = ChefFollow.builder()
                .follower(follower)
                .following(following)
                .build();

        followRepository.save(chefFollow);
    }

    @Transactional
    public void unfollowChef(UUID chefIdToUnfollow, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User Not found"));

        Chef follower = user.getChef();

        if (follower == null) {
            throw new IllegalStateException("User is not a chef");
        }

        if (!followRepository.existsByFollowerIdAndFollowingId(follower.getId(), chefIdToUnfollow)) {
            throw new IllegalStateException("Not following this chef");
        }

        followRepository.deleteByFollowerIdAndFollowingId(follower.getId(), chefIdToUnfollow);
    }

    @Transactional
    public List<UUID> getFollowingChefIds(UUID chefId) {
        return followRepository.findFollowingChefIds(chefId);
    }

    @Transactional
    public PaginatedResponse<ChefResponse> getFollowers(UUID chefId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<ChefFollow> followPage = followRepository.findByFollowingId(chefId, pageable);

        List<ChefResponse> content = followPage.getContent().stream()
                .map(follow -> mapToChefResponse(follow.getFollower(), null))
                .collect(Collectors.toList());

        return buildPaginatedResponse(content, followPage, page, pageSize);
    }

    @Transactional
    public PaginatedResponse<ChefResponse> getFollowing(UUID chefId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<ChefFollow> followPage = followRepository.findByFollowerId(chefId, pageable);

        List<ChefResponse> content = followPage.getContent().stream()
                .map(follow -> mapToChefResponse(follow.getFollowing(), null))
                .collect(Collectors.toList());

        return buildPaginatedResponse(content, followPage, page, pageSize);
    }

    @Transactional
    public ChefResponse getChefProfile(UUID chefId, String userEmail) {
        Chef chef = chefRepository.findById(chefId)
                .orElseThrow(() -> new ResourceNotFoundException("Chef Not Found"));

        Boolean isFollowing = null;

        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);

            if (user != null && user.getChef() != null) {
                isFollowing = followRepository.existsByFollowerIdAndFollowingId(user.getChef().getId(), chefId);
            }
        }

        return mapToChefResponse(chef, isFollowing);
    }

    private ChefResponse mapToChefResponse(Chef chef, Boolean isFollowing) {
        Long recipesCount = chefRepository.countPublishedRecipesChefId(chef.getId());
        Long followersCount = followRepository.countByFollowingId(chef.getId());
        Long followingCount = followRepository.countByFollowerId(chef.getId());

        return ChefResponse.builder()
                .id(chef.getId().toString())
                .handle(chef.getHandle())
                .firstName(chef.getUser().getFirstName())
                .lastName(chef.getUser().getLastName())
                .bio(chef.getBio())
                .profileImageUrl(chef.getProfileImageUrl())
                .recipesCount(recipesCount)
                .followersCount(followersCount)
                .followingCount(followingCount)
                .isFollowing(isFollowing)
                .createdAt(chef.getUser().getCreatedAt())
                .build();
    }

    private <T> PaginatedResponse<T> buildPaginatedResponse(List<T> content, Page<?> page, int pageNum, int pageSize) {
        return PaginatedResponse.<T>builder()
                .content(content)
                .page(pageNum)
                .pageSize(pageSize)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}