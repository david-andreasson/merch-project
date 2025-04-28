package com.jin12.reviews_api.dto;

import com.jin12.reviews_api.model.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRespons {
    private String productId;
    private String productName;
    private String category;
    private String tags;
    private List<Review> reviews;
}
