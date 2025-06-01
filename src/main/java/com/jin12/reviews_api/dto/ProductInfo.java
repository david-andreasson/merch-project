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
    private String name;
    private String category;
    private List<String> tags;

    /**
     * Returns the product name from the 'name' field.
     * This maps the external API's 'name' property to our internal naming.
     *
     * @return the product name
     */
    public String getProductName() {
        return name;
    }

    /**
     * Sets the product name into the 'name' field.
     * Used to map external JSON 'name' to this DTO.
     *
     * @param name the product name to set
     */
    public void setProductName(String name) {
        this.name = name;
    }
}