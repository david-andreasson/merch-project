package com.jin12.reviews_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRespons {
    private String productId;
    private String productName;
    private String category;
    private String tags;
}
