package com.example.RecipeBook.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "E-mail is required")
    @Email(message = "Invalid e-mail format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}

