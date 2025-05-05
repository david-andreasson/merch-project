package com.jin12.reviews_api.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductInfoDtoTest {
    @Test
    void testGettersAndSetters() {
        ProductInfoDto dto = new ProductInfoDto();
        dto.setProductName("Bil");
        dto.setCategory("Car");
        dto.setTags(java.util.List.of("C30", "Grön"));
        assertEquals("Bil", dto.getProductName());
        assertEquals("Car", dto.getCategory());
        assertEquals(java.util.List.of("C30", "Grön"), dto.getTags());
    }

    @Test
    void testEqualsAndHashCode() {
        ProductInfoDto dto1 = new ProductInfoDto();
        dto1.setProductName("Bil");
        dto1.setCategory("Car");
        dto1.setTags(java.util.List.of("C30"));
        ProductInfoDto dto2 = new ProductInfoDto();
        dto2.setProductName("Bil");
        dto2.setCategory("Car");
        dto2.setTags(java.util.List.of("C30"));
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        ProductInfoDto dto = new ProductInfoDto();
        dto.setProductName("Bil");
        dto.setCategory("Car");
        dto.setTags(java.util.List.of("C30"));
        assertTrue(dto.toString().contains("Bil"));
        assertTrue(dto.toString().contains("Car"));
    }
}
