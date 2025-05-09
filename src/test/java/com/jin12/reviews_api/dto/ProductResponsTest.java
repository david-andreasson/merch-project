package com.jin12.reviews_api.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductResponsTest {
    @org.junit.jupiter.api.Test
    void testGettersAndSetters() {
        ProductRespons dto = new ProductRespons();
        dto.setProductId("P1");
        dto.setProductName("Bil");
        dto.setCategory("Car");
        dto.setTags("C30,Grön");
        org.junit.jupiter.api.Assertions.assertEquals("P1", dto.getProductId());
        org.junit.jupiter.api.Assertions.assertEquals("Bil", dto.getProductName());
        org.junit.jupiter.api.Assertions.assertEquals("Car", dto.getCategory());
        org.junit.jupiter.api.Assertions.assertEquals("C30,Grön", dto.getTags());
    }
    @org.junit.jupiter.api.Test
    void testEqualsAndHashCode() {
        ProductRespons dto1 = new ProductRespons();
        dto1.setProductId("P1");
        dto1.setProductName("Bil");
        dto1.setCategory("Car");
        dto1.setTags("C30,Grön");
        ProductRespons dto2 = new ProductRespons();
        dto2.setProductId("P1");
        dto2.setProductName("Bil");
        dto2.setCategory("Car");
        dto2.setTags("C30,Grön");
        org.junit.jupiter.api.Assertions.assertEquals(dto1, dto2);
        org.junit.jupiter.api.Assertions.assertEquals(dto1.hashCode(), dto2.hashCode());
    }
    @org.junit.jupiter.api.Test
    void testToString() {
        ProductRespons dto = new ProductRespons();
        dto.setProductName("Bil");
        dto.setCategory("Car");
        dto.setTags("C30,Grön");
        org.junit.jupiter.api.Assertions.assertTrue(dto.toString().contains("Bil"));
        org.junit.jupiter.api.Assertions.assertTrue(dto.toString().contains("Car"));
    }
}
