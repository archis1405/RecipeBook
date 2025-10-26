package com.example.RecipeBook.exception;

import com.example.RecipeBook.exception.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex , HttpServletRequest request){
        ErrorResponse error = ErrorResponse.builder().message(ex.getMessage()).
                error("Not Found").status(HttpStatus.NOT_FOUND.value()).
                timestamp(LocalDateTime.now()).path(request.getRequestURI()).build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex , HttpServletRequest request){
        ErrorResponse error = ErrorResponse.builder().message(ex.getMessage()).
                error("Unauthorized").status(HttpStatus.FORBIDDEN.value()).
                timestamp(LocalDateTime.now()).path(request.getRequestURI()).build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex , HttpServletRequest request){
        ErrorResponse error = ErrorResponse.builder().message("Invalid Email and Password").
                error("Unauthorized").status(HttpStatus.UNAUTHORIZED.value()).
                timestamp(LocalDateTime.now()).path(request.getRequestURI()).build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex , HttpServletRequest request){
        ErrorResponse error = ErrorResponse.builder().message(ex.getMessage()).
                error("Bad Reequest").status(HttpStatus.BAD_REQUEST.value()).
                timestamp(LocalDateTime.now()).path(request.getRequestURI()).build();

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HashMap<String , Object>> handleValidationExceptions(MethodArgumentNotValidException ex , HttpServletRequest request){
        HashMap<String , Object> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName , errorMessage);
        });

        HashMap<String , Object> response = new HashMap<>();
        response.put("errors" , errors);
        response.put("status" , HttpStatus.BAD_REQUEST.value());
        response.put("timestamp" , LocalDateTime.now());
        response.put("path" , request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex , HttpServletRequest request){
        ErrorResponse error = ErrorResponse.builder().message("An unexpected error occured").
                error("Internal Server Error").status(HttpStatus.INTERNAL_SERVER_ERROR.value()).
                timestamp(LocalDateTime.now()).path(request.getRequestURI()).build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
