package com.jin12.reviews_api.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "reviews")
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
    private Product product;
}
