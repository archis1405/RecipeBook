package com.example.RecipeBook.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private  String error;
    private int status;
    private LocalDateTime timestamp;
    private String path;
}
