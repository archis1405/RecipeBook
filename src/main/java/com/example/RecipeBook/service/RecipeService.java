package com.example.RecipeBook.service;

import com.example.RecipeBook.dto.RecipeMessage;
import com.example.RecipeBook.dto.request.RecipeRequest;
import com.example.RecipeBook.dto.response.PaginatedResponse;
import com.example.RecipeBook.dto.response.RecipeResponse;
import com.example.RecipeBook.entity.Chef;
import com.example.RecipeBook.entity.Recipe;
import com.example.RecipeBook.entity.RecipeImage;
import com.example.RecipeBook.entity.User;
import com.example.RecipeBook.exception.ResourceNotFoundException;
import com.example.RecipeBook.exception.UnauthorizedException;
import com.example.RecipeBook.repository.ChefRepository;
import com.example.RecipeBook.repository.RecipeImageRepository;
import com.example.RecipeBook.repository.RecipeRepository;
import com.example.RecipeBook.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final ChefRepository chefRepository;
    private final UserRepository userRepository;
    private final RecipeImageRepository imageRepository;
    private final FollowService followService;
    private final ImageService imageService;
    private final MessageQueueService messageQueueService;

    @Transactional
    public RecipeResponse createRecipe(Recipe request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found "));

        Chef chef = user.getChef();

        if (chef == null) {
            throw new UnauthorizedException("Only chefs can create recipes");
        }

        Recipe recipe = Recipe.builder()
                .chef(chef)
                .title(request.getTitle())
                .summary(request.getSummary())
                .ingredients(request.getIngredients())
                .steps(request.getSteps())
                .labels(request.getLabels())
                .preparationTime(request.getPreparationTime())
                .cookingTime(request.getCookingTime())
                .servings(request.getServings())
                .status(Recipe.RecipeStatus.DRAFT)
                .build();

        recipe = recipeRepository.save(recipe);

        return mapToResponse(recipe);
    }

    @Transactional
    public RecipeResponse publishRecipe(UUID recipeId, String userEmail) {
        Recipe recipe = getRecipe(recipeId);
        validateOwnership(recipe, userEmail);

        if (recipe.getStatus() == Recipe.RecipeStatus.PUBLISHED) {
            throw new IllegalStateException("Recipe is already published");
        }

        recipe.setStatus(Recipe.RecipeStatus.PROCESSING);
        recipe = recipeRepository.save(recipe);

        RecipeMessage message = buildRecipeMessage(recipe);
        messageQueueService.sendRecipeForProcessing(message);

        return mapToResponse(recipe);
    }

    @Transactional
    public PaginatedResponse<RecipeResponse> getRecipesFromFollowedChefs(String userEmail, String keyword,
                                                                         String publishedFrom, String publishedTo, String chefId, String chefHandle,
                                                                         int page, int pageSize) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found "));

        Chef currentChef = user.getChef();
        if (currentChef == null) {
            throw new UnauthorizedException("User is not a chef");
        }

        List<UUID> followedChefIds = followService.getFollowingChefIds(currentChef.getId());

        if (followedChefIds.isEmpty()) {
            return PaginatedResponse.<RecipeResponse>builder()
                    .content(List.of())
                    .page(page)
                    .pageSize(pageSize)
                    .totalElements(0L)
                    .totalPages(0)
                    .last(true)
                    .first(true)
                    .build();
        }

        LocalDateTime fromDate = publishedFrom != null ? LocalDateTime.parse(publishedFrom, DateTimeFormatter.ISO_DATE_TIME) : null;

        LocalDateTime toDate = publishedTo != null ? LocalDateTime.parse(publishedTo, DateTimeFormatter.ISO_DATE_TIME) : null;

        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Recipe> recipePage = recipeRepository.findRecipesFromFollowedChefs(followedChefIds, keyword, fromDate, toDate, pageable);

        List<RecipeResponse> content = recipePage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PaginatedResponse.<RecipeResponse>builder()
                .content(content)
                .page(page)
                .pageSize(pageSize)
                .totalElements(recipePage.getTotalElements())
                .totalPages(recipePage.getTotalPages())
                .last(recipePage.isLast())
                .first(recipePage.isFirst())
                .build();
    }

    @Transactional
    public RecipeResponse getRecipeById(UUID id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe Not Found "));

        return mapToResponse(recipe);
    }

    @Transactional
    public RecipeResponse updateRecipe(UUID id, RecipeRequest request, String userEmail) {
        Recipe recipe = getRecipe(id);
        validateOwnership(recipe, userEmail);

        recipe.setTitle(request.getTitle());
        recipe.setSummary(request.getSummary());
        recipe.setIngredients(request.getIngredients());
        recipe.setSteps(request.getSteps());
        recipe.setLabels(request.getLabels());
        recipe.setPreparationTime(request.getPreparationTime());
        recipe.setCookingTime(request.getCookingTime());
        recipe.setServings(request.getServings());

        recipe = recipeRepository.save(recipe);
        return mapToResponse(recipe);
    }

    @Transactional
    public void deleteRecipe(UUID id, String userEmail) {
        Recipe recipe = getRecipe(id);
        validateOwnership(recipe, userEmail);
        recipeRepository.delete(recipe);
    }

    @Transactional
    public RecipeResponse uploadImages(UUID recipeId, List<MultipartFile> files, String userEmail) {
        Recipe recipe = getRecipe(recipeId);
        validateOwnership(recipe, userEmail);

        int displayOrder = recipe.getRecipeImages().size();

        for (MultipartFile file : files) {
            String originalUrl = imageService.saveImage(file);

            RecipeImage image = RecipeImage.builder()
                    .recipe(recipe)
                    .originalUrl(originalUrl)
                    .displayOrder(displayOrder++)
                    .build();

            recipe.getRecipeImages().add(image);
        }

        recipe = recipeRepository.save(recipe);
        return mapToResponse(recipe);
    }

    private Recipe getRecipe(UUID id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe Not Found "));
    }

    private void validateOwnership(Recipe recipe, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        if (user.getRole() != User.Role.ADMIN && !recipe.getChef().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to perform this action");
        }
    }

    private RecipeMessage buildRecipeMessage(Recipe recipe) {
        List<RecipeMessage.ImageInfo> imageInfos = recipe.getRecipeImages().stream()
                .map(img -> RecipeMessage.ImageInfo.builder()
                        .imageId(img.getId().toString())
                        .originalUrl(img.getOriginalUrl())
                        .displayOrder(img.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());

        return RecipeMessage.builder()
                .recipeId(recipe.getId().toString())
                .chefId(recipe.getChef().getId().toString())
                .title(recipe.getTitle())
                .summary(recipe.getSummary())
                .ingredients(recipe.getIngredients())
                .steps(recipe.getSteps())
                .labels(recipe.getLabels())
                .preparationTime(recipe.getPreparationTime())
                .cookingTime(recipe.getCookingTime())
                .servings(recipe.getServings())
                .images(imageInfos)
                .build();
    }

    private RecipeResponse mapToResponse(Recipe recipe) {
        List<String> imageUrls = recipe.getRecipeImages().stream()
                .sorted((a, b) -> a.getDisplayOrder().compareTo(b.getDisplayOrder()))
                .map(img -> img.getLargeUrl() != null ? img.getLargeUrl() : img.getOriginalUrl())
                .collect(Collectors.toList());

        RecipeResponse.ChefInfo chefInfo = RecipeResponse.ChefInfo.builder()
                .id(recipe.getChef().getId().toString())
                .handle(recipe.getChef().getHandle())
                .firstName(recipe.getChef().getUser().getFirstName())
                .lastName(recipe.getChef().getUser().getLastName())
                .profileImageUrl(recipe.getChef().getProfileImageUrl())
                .build();

        return RecipeResponse.builder()
                .id(recipe.getId().toString())
                .title(recipe.getTitle())
                .summary(recipe.getSummary())
                .ingredients(recipe.getIngredients())
                .steps(recipe.getSteps())
                .labels(recipe.getLabels())
                .preparationTime(recipe.getPreparationTime())
                .cookingTime(recipe.getCookingTime())
                .servings(recipe.getServings())
                .status(recipe.getStatus().name())
                .publishedAt(recipe.getPublishedAt())
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .chef(chefInfo)
                .imageUrls(imageUrls)
                .build();
    }
}