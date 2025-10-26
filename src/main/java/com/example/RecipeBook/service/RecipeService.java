package com.example.RecipeBook.service;

import com.example.RecipeBook.repository.ChefRepository;
import com.example.RecipeBook.repository.RecipeImageRepository;
import com.example.RecipeBook.repository.RecipeRepository;
import com.example.RecipeBook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final ChefRepository chefRepository;
    private final UserRepository userRepository;
    private final RecipeImageRepository imageRepository;


}
