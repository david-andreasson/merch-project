package com.jin12.reviews_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Product entity representing a product record.
 * productId is a combination of userId and client-visible ID to avoid collisions.
 * Cascade and orphanRemoval ensure that removing a Product also removes its Reviews.
 */
@Entity
@Data
@Builder
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @Column(nullable = false)
    private String productId;

    @Column(nullable = true)
    private String productName;

    @Column(nullable = true)
    private String category;

    //Comma seperated values
    @Column(nullable = true)
    private String tags;

    /**
     * List of reviews for this product.
     * orphanRemoval=true deletes reviews when the product is deleted.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}