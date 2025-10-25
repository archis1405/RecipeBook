package com.example.RecipeBook.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    @NotBlank(message = "E-mail is required")
    @Email(message = "Invalid e-mail format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "Handle must be 3-20 characters and can only contain letters, numbers, and underscores")
    private String handle;

    @NotBlank(message = "Role is required")
    private String role;

    private String bio;
}
