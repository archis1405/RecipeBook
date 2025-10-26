package com.example.RecipeBook.service;

import com.example.RecipeBook.entity.User;
import com.example.RecipeBook.repository.ChefFollowRepository;
import com.example.RecipeBook.repository.ChefRepository;
import com.example.RecipeBook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final ChefFollowRepository followRepository;
    private final ChefRepository chefRepository;
    private final UserRepository userRepository;

    public void followChef(UUID chefIdToFollow , String userEmail){
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new )
    }
}
