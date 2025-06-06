package com.jin12.reviews_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ProductRequest {
    private String mode;
    private String productId;
    private String productInfoUrl;
    private String productName;
    private String category;
    private List<String> tags;
    private ReviewRequest review;
}
