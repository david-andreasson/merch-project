package com.jin12.reviews_api.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ProductInfo {
    private String mode;
    private String productId;
    private String productInfoUrl;
    private String productName;
    private String category;
    private List<String> tags;
    private Review review;
}
