package com.jin12.reviews_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductInfoDto {
    private String productName;
    private String category;
    private List<String> tags;
}