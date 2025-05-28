package com.jin12.reviews_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    // Tar man bort en product kommer alla reviews att tas bort med hjälp av cascade och orphanRemoval
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
