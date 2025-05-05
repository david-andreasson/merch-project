package com.jin12.reviews_api.repository;

import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository; // Krävs för att sätta relationen

    @Test
    @DisplayName("Should find product by productId")
    void testFindByProductId() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user = userRepository.save(user);

        Product product = new Product();
        product.setProductId("prod-123");
        product.setProductName("Test Product");
        product.setUser(user);

        productRepository.save(product);

        // Act
        Optional<Product> result = productRepository.findByProductId("prod-123");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getProductName()).isEqualTo("Test Product");
        assertThat(result.get().getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should find products by userId")
    void testFindByUserId() {
        // Arrange
        User user = new User();
        user.setUsername("multiuser");
        user.setPassword("secure");
        user = userRepository.save(user);

        Product product1 = new Product();
        product1.setProductId("p1");
        product1.setProductName("First");
        product1.setUser(user);

        Product product2 = new Product();
        product2.setProductId("p2");
        product2.setProductName("Second");
        product2.setUser(user);

        productRepository.saveAll(List.of(product1, product2));

        // Act
        List<Product> result = productRepository.findByUserId(user.getId());

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::getProductId).containsExactlyInAnyOrder("p1", "p2");
    }

    @Test
    @DisplayName("Should return empty when productId not found")
    void testFindByProductIdNotFound() {
        Optional<Product> result = productRepository.findByProductId("does-not-exist");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list for unknown userId")
    void testFindByUserIdNotFound() {
        List<Product> result = productRepository.findByUserId(999L);
        assertThat(result).isEmpty();
    }
}