package com.example.RecipeBook.repository;

import com.example.RecipeBook.entity.ChefFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChefFollowRepository extends JpaRepository<ChefFollow , UUID> {
    Optional<ChefFollow> findByFollowerAndFollowingId(UUID followerId , UUID followingId);
    boolean existsByFollowerIdAndFollowingId(UUID followerId , UUID followingId);
    void deleteByFollowerIdAndFollowingId(UUID followerId , UUID followingId);

    @Query("SELECT cf.following.id FROM ChefFollow ch WHERE cf.follower.id = :chefId")
    List<UUID> findFollowingChefIds(UUID chefId);

    Long countByFollowerId(UUID followerId);
    Long countByFollowingId(UUID followingId);

    Page<ChefFollow> findByFollowerId(UUID followerId , Pageable pageable);
    Page<ChefFollow> findByFollowingId(UUID followingId , Pageable pageable);
}
