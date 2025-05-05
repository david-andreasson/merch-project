package com.jin12.reviews_api.repository;

import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.model.Review;
import com.jin12.reviews_api.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should find reviews by product and date after")
    void testFindByProductAndDateAfter() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("secret");
        user = userRepository.save(user);

        Product product = new Product();
        product.setProductId("prod-1");
        product.setProductName("Cool Widget");
        product.setUser(user);
        product = productRepository.save(product);

        Review oldReview = new Review("Old Reviewer", "Old review", 4, false);
        oldReview.setProduct(product);
        oldReview.setDate(LocalDate.now().minusMonths(3));

        Review recentReview = new Review("New Reviewer", "New review", 5, false);
        recentReview.setProduct(product);
        recentReview.setDate(LocalDate.now().minusDays(10));

        reviewRepository.saveAll(List.of(oldReview, recentReview));

        List<Review> result = reviewRepository.findByProductAndDateAfter(product, LocalDate.now().minusMonths(2));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("New Reviewer");
    }

    @Test
    @DisplayName("Should return top 10 reviews ordered by date descending")
    void testFindTop10ByProductOrderByDateDesc() {
        User user = new User();
        user.setUsername("topuser");
        user.setPassword("123");
        user = userRepository.save(user);

        Product product = new Product();
        product.setProductId("top-prod");
        product.setProductName("Fancy Product");
        product.setUser(user);
        product = productRepository.save(product);

        for (int i = 1; i <= 15; i++) {
            Review review = new Review("Reviewer " + i, "Review text " + i, i % 5 + 1, false);
            review.setProduct(product);
            review.setDate(LocalDate.now().minusDays(i));
            reviewRepository.save(review);
        }

        List<Review> result = reviewRepository.findTop10ByProductOrderByDateDesc(product);

        assertThat(result).hasSize(10);
        assertThat(result.get(0).getDate()).isAfter(result.get(9).getDate()); // Kontrollera sortering
    }
}