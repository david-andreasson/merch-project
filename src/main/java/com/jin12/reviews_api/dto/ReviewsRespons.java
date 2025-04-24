package com.jin12.reviews_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewsRespons {
    private String productId;
    private double averageRating;
    private List<ReviewRespons> reviews;
}
