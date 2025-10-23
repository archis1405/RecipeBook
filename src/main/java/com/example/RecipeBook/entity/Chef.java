package com.example.RecipeBook.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "chefs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chef {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false , unique = true)
    private String handle;

    @Column(columnDefinition = "TEXT")
    private String bio;
    private String profileImageUrl;
}
