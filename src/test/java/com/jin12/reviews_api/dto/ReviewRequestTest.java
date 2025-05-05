package com.jin12.reviews_api.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReviewRequestTest {
    @Test
    void testGettersAndSetters() {
        ReviewRequest dto = new ReviewRequest();
        dto.setName("Kalle");
        dto.setText("Bra produkt");
        dto.setRating(5);
        assertEquals("Kalle", dto.getName());
        assertEquals("Bra produkt", dto.getText());
        assertEquals(5, dto.getRating());
    }
    @Test
    void testEqualsAndHashCode() {
        ReviewRequest dto1 = new ReviewRequest();
        dto1.setName("Kalle");
        dto1.setText("Bra produkt");
        dto1.setRating(5);
        ReviewRequest dto2 = new ReviewRequest();
        dto2.setName("Kalle");
        dto2.setText("Bra produkt");
        dto2.setRating(5);
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
    @Test
    void testToString() {
        ReviewRequest dto = new ReviewRequest();
        dto.setName("Kalle");
        dto.setText("Bra produkt");
        dto.setRating(5);
        assertTrue(dto.toString().contains("Bra produkt"));
    }
}
