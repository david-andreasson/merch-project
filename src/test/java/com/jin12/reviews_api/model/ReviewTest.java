package com.jin12.reviews_api.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewTest {
    @Test
    void testNoArgsConstructorAndSetters() {
        Review review = new Review();

        Product product = new Product();
        LocalDate today = LocalDate.now();

        review.setId(1L);
        review.setName("Test User");
        review.setReviewText("Great product!");
        review.setRating(5);
        review.setDate(today);
        review.setGeneratedByAI(true);
        review.setProduct(product);

        assertThat(review.getId()).isEqualTo(1L);
        assertThat(review.getName()).isEqualTo("Test User");
        assertThat(review.getReviewText()).isEqualTo("Great product!");
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getDate()).isEqualTo(today);
        assertThat(review.isGeneratedByAI()).isTrue();
        assertThat(review.getProduct()).isSameAs(product);
    }

    @Test
    void testCustomConstructor() {
        Review review = new Review("Alice", "Very nice", 4, false);

        assertThat(review.getName()).isEqualTo("Alice");
        assertThat(review.getReviewText()).isEqualTo("Very nice");
        assertThat(review.getRating()).isEqualTo(4);
        assertThat(review.isGeneratedByAI()).isFalse();

        // Dessa bör vara null eftersom de inte sätts av konstruktorn
        assertThat(review.getId()).isNull();
        assertThat(review.getDate()).isNull();
        assertThat(review.getProduct()).isNull();
    }

    @Test
    void testEqualsAndHashCode() {
        Review review1 = new Review("Bob", "Nice!", 3, false);
        Review review2 = new Review("Bob", "Nice!", 3, false);

        review1.setId(1L);
        review2.setId(1L);

        assertThat(review1).isEqualTo(review2);
        assertThat(review1.hashCode()).isEqualTo(review2.hashCode());
    }

    @Test
    void testToString() {
        Review review = new Review("Clara", "Wow!", 5, true);
        review.setId(10L);
        review.setDate(LocalDate.of(2023, 10, 1));

        String toString = review.toString();
        assertThat(toString).contains("Clara", "Wow!", "5", "true");
        assertThat(toString).doesNotContain("Product"); // eftersom det är @JsonIgnore och null
    }
}