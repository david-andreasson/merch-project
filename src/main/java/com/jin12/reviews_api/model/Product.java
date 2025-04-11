package com.jin12.reviews_api.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String category;

//    @Column(nullable = false)
//    private Set<String> tags;

    @Column(nullable = false)
    private String tags;

    @OneToMany
    private List<Review> reviews;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
