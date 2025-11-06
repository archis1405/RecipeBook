package com.example.RecipeBook.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public class SecurityUtils {


        public static Optional<String> getCurrentUserEmail() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                return Optional.of(userDetails.getUsername());
            }

            return Optional.empty();
        }

        public static boolean hasRole(String role) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getAuthorities() != null) {
                return authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
            }

            return false;
        }


        public static boolean isAuthenticated() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null && authentication.isAuthenticated();
        }

        public static Optional<Authentication> getCurrentAuthentication() {
            return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
        }

}
