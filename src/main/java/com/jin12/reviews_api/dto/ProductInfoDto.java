package com.jin12.reviews_api.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductInfoDto {
    private String productName;
    private String category;
    private List<String> tags;

    // This DTO is used internally to transfer product details within the application
    // (separate from the external ProductInfo DTO which maps JSON from the external service)
}