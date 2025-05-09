package com.jin12.reviews_api.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReviewStatsResponseTest {
    @Test
    void testGettersAndSetters() {
        ReviewStatsResponse dto = new ReviewStatsResponse();
        dto.setProductId("P1");
        dto.setProductName("Volvo");
        dto.setCurrentAverage(4.5);
        dto.setTotalReviews(10);
        dto.setLastReviewDate("2024-05-05");
        assertEquals("P1", dto.getProductId());
        assertEquals("Volvo", dto.getProductName());
        assertEquals(4.5, dto.getCurrentAverage());
        assertEquals(10, dto.getTotalReviews());
        assertEquals("2024-05-05", dto.getLastReviewDate());
    }
    @Test
    void testEqualsAndHashCode() {
        ReviewStatsResponse dto1 = new ReviewStatsResponse();
        dto1.setProductId("P1");
        dto1.setProductName("Volvo");
        dto1.setCurrentAverage(4.5);
        dto1.setTotalReviews(10);
        dto1.setLastReviewDate("2024-05-05");
        ReviewStatsResponse dto2 = new ReviewStatsResponse();
        dto2.setProductId("P1");
        dto2.setProductName("Volvo");
        dto2.setCurrentAverage(4.5);
        dto2.setTotalReviews(10);
        dto2.setLastReviewDate("2024-05-05");
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
    @Test
    void testToString() {
        ReviewStatsResponse dto = new ReviewStatsResponse();
        dto.setProductName("Volvo");
        dto.setCurrentAverage(4.5);
        assertTrue(dto.toString().contains("Volvo"));
        assertTrue(dto.toString().contains("4.5"));
    }
}
