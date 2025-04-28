package com.jin12.reviews_api.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "reviews")
@NoArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String reviewText;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private boolean generatedByAI;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    public Review(String name, String reviewText, int rating, boolean generatedByAI) {
        this.name = name;
        this.reviewText = reviewText;
        this.rating = rating;
        this.generatedByAI = generatedByAI;
    }
}
