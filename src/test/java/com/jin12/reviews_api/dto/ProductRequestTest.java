package com.jin12.reviews_api.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class ProductRequestTest {
    @org.junit.jupiter.api.Test
    void testGettersAndSetters() {
        ProductRequest dto = new ProductRequest();
        dto.setMode("create");
        dto.setProductId("P1");
        dto.setProductInfoUrl("http://example.com");
        dto.setProductName("Bil");
        dto.setCategory("Car");
        dto.setTags(List.of("C30", "Grön"));
        ReviewRequest review = new ReviewRequest();
        review.setName("Kalle");
        dto.setReview(review);
        org.junit.jupiter.api.Assertions.assertEquals("create", dto.getMode());
        org.junit.jupiter.api.Assertions.assertEquals("P1", dto.getProductId());
        org.junit.jupiter.api.Assertions.assertEquals("http://example.com", dto.getProductInfoUrl());
        org.junit.jupiter.api.Assertions.assertEquals("Bil", dto.getProductName());
        org.junit.jupiter.api.Assertions.assertEquals("Car", dto.getCategory());
        org.junit.jupiter.api.Assertions.assertEquals(List.of("C30", "Grön"), dto.getTags());
        org.junit.jupiter.api.Assertions.assertEquals(review, dto.getReview());
    }
    @org.junit.jupiter.api.Test
    void testEqualsAndHashCode() {
        ProductRequest dto1 = new ProductRequest();
        ProductRequest dto2 = new ProductRequest();
        org.junit.jupiter.api.Assertions.assertEquals(dto1, dto2);
        org.junit.jupiter.api.Assertions.assertEquals(dto1.hashCode(), dto2.hashCode());
    }
    @org.junit.jupiter.api.Test
    void testToString() {
        ProductRequest dto = new ProductRequest();
        dto.setProductName("Bil");
        org.junit.jupiter.api.Assertions.assertTrue(dto.toString().contains("Bil"));
    }
}
