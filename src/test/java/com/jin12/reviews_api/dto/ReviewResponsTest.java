package com.jin12.reviews_api.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

class ReviewResponsTest {
    @Test
    void testGettersAndSetters() {
        ReviewRespons dto = new ReviewRespons();
        LocalDate date = LocalDate.of(2024, 5, 5);
        dto.setDate(date);
        dto.setName("Kalle");
        dto.setRating(5);
        dto.setText("Bra produkt");
        assertEquals(date, dto.getDate());
        assertEquals("Kalle", dto.getName());
        assertEquals(5, dto.getRating());
        assertEquals("Bra produkt", dto.getText());
    }
    @Test
    void testEqualsAndHashCode() {
        LocalDate date = LocalDate.of(2024, 5, 5);
        ReviewRespons dto1 = new ReviewRespons();
        dto1.setDate(date);
        dto1.setName("Kalle");
        dto1.setRating(5);
        dto1.setText("Bra produkt");
        ReviewRespons dto2 = new ReviewRespons();
        dto2.setDate(date);
        dto2.setName("Kalle");
        dto2.setRating(5);
        dto2.setText("Bra produkt");
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
    @Test
    void testToString() {
        ReviewRespons dto = new ReviewRespons();
        dto.setName("Kalle");
        dto.setText("Bra produkt");
        assertTrue(dto.toString().contains("Bra produkt"));
    }
}
