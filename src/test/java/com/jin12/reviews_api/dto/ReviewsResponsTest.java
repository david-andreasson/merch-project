package com.jin12.reviews_api.dto;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ReviewsResponsTest {
    @Test
    void testGettersAndSetters() {
        ReviewsRespons dto = new ReviewsRespons();
        dto.setProductId("P1");
        ReviewStatsResponse stats = new ReviewStatsResponse();
        stats.setProductId("P1");
        dto.setStats(stats);
        ReviewRespons review = new ReviewRespons();
        review.setName("Kalle");
        review.setText("Bra produkt");
        review.setRating(5);
        review.setDate(LocalDate.of(2024, 5, 5));
        dto.setReviews(List.of(review));
        assertEquals("P1", dto.getProductId());
        assertEquals(stats, dto.getStats());
        assertEquals(List.of(review), dto.getReviews());
    }

    @Test
    void testEqualsAndHashCode() {
        ReviewsRespons dto1 = new ReviewsRespons();
        ReviewsRespons dto2 = new ReviewsRespons();
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        ReviewsRespons dto = new ReviewsRespons();
        dto.setProductId("P1");
        assertTrue(dto.toString().contains("P1"));
    }
}
