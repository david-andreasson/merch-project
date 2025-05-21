package com.jin12.reviews_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class BasicProductDto {
    private String id;
    private String name;
    private String description;
    private Double price;
    private String currency;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("stock_quantity")
    private Integer stockQuantity;

    @JsonProperty("in_stock")
    private Boolean inStock;

    private String category;
    private List<String> tags;
}