package com.questie.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "words")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String value;

    @Enumerated(EnumType.STRING)
    private WordDifficulty difficulty;

    @Column(nullable = false)
    private String category;

    public enum WordDifficulty {
        EASY,
        MEDIUM,
        HARD
    }
}