package com.example.RecipeBook.service;

import com.example.RecipeBook.dto.request.LoginRequest;
import com.example.RecipeBook.dto.request.SignUpRequest;
import com.example.RecipeBook.dto.response.AuthResponse;
import com.example.RecipeBook.entity.Chef;
import com.example.RecipeBook.entity.User;
import com.example.RecipeBook.exception.ResourceNotFoundException;
import com.example.RecipeBook.repository.ChefRepository;
import com.example.RecipeBook.repository.UserRepository;
import com.example.RecipeBook.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final ChefRepository chefRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse signUp(SignUpRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new IllegalStateException("Email already exists");
        }

        if(request.getHandle() != null && chefRepository.existsByHandle(request.getHandle())){
            throw new IllegalStateException("Handle already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())  // Fixed: was getFirstName()
                .role(User.Role.valueOf(request.getRole().toUpperCase()))
                .enabled(true)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        Chef chef = null;
        if(user.getRole() == User.Role.CHEF || user.getRole() == User.Role.ADMIN){
            chef = Chef.builder()
                    .user(user)
                    .handle(request.getHandle())
                    .bio(request.getBio())
                    .build();

            chef = chefRepository.save(chef);
            user.setChef(chef);
        }

        // Fixed: Use Spring Security's Authentication, not Tomcat's
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Fixed: Pass user and chef to buildAuthResponse
        return buildAuthResponse(authentication, user, chef);
    }

    private AuthResponse login(LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new ResourceNotFoundException("user Not Found"));

        Chef chef = user.getChef();

        return buildAuthResponse(authentication,user,chef);
    }

    private AuthResponse buildAuthResponse(Authentication authentication, User user, Chef chef) {
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .chefId(chef != null ? chef.getId().toString() : null)
                .handle(chef != null ? chef.getHandle() : null)
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationTime() / 1000)
                .user(userInfo)
                .build();
    }
}