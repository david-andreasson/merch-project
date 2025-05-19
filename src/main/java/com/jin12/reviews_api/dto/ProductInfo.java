package com.jin12.reviews_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfo {
    public String getProductName() {
        return name;
    }

    public void setProductName(String name) {
        this.name = name;
    }

    private String name;
    private String category;
    private List<String> tags;
}
