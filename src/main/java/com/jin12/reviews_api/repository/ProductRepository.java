package com.jin12.reviews_api.repository;

import com.jin12.reviews_api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface ProductRepository extends JpaRepository<Product, String> {

    Optional<Product> findByProductId(String productId);

    List<Product> findByUserId(Long userId);


}
