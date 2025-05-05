package com.jin12.reviews_api.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        Product product = new Product();
        product.setProductId("123");
        product.setProductName("TestProduct");
        product.setCategory("Electronics");
        product.setTags("tech, gadget");

        User user = new User();
        user.setId(1L);
        product.setUser(user);

        List<Review> reviews = new ArrayList<>();
        Review review = new Review();
        review.setId(100L);
        review.setProduct(product);
        reviews.add(review);
        product.setReviews(reviews);

        assertEquals("123", product.getProductId());
        assertEquals("TestProduct", product.getProductName());
        assertEquals("Electronics", product.getCategory());
        assertEquals("tech, gadget", product.getTags());
        assertEquals(user, product.getUser());
        assertEquals(1, product.getReviews().size());
    }

    @Test
    void testAllArgsConstructor() {
        User user = new User();
        user.setId(2L);

        List<Review> reviews = new ArrayList<>();
        Product product = new Product("456", "Phone", "Mobiles", "smartphone", reviews, user);

        assertEquals("456", product.getProductId());
        assertEquals("Phone", product.getProductName());
        assertEquals("Mobiles", product.getCategory());
        assertEquals("smartphone", product.getTags());
        assertEquals(reviews, product.getReviews());
        assertEquals(user, product.getUser());
    }

    @Test
    void testBuilder() {
        User user = new User();
        user.setId(3L);

        Product product = Product.builder()
                .productId("789")
                .productName("Tablet")
                .category("Electronics")
                .tags("portable")
                .user(user)
                .reviews(new ArrayList<>())
                .build();

        assertEquals("789", product.getProductId());
        assertEquals("Tablet", product.getProductName());
        assertEquals("Electronics", product.getCategory());
        assertEquals("portable", product.getTags());
        assertEquals(user, product.getUser());
        assertTrue(product.getReviews().isEmpty());
    }

    @Test
    void testToStringAndEqualsHashCode() {
        Product p1 = Product.builder()
                .productId("1")
                .productName("Item")
                .build();

        Product p2 = Product.builder()
                .productId("1")
                .productName("Item")
                .build();

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertTrue(p1.toString().contains("Item"));
    }
}