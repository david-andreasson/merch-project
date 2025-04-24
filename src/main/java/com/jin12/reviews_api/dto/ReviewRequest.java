package com.jin12.reviews_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReviewRequest {
    private String name;
    private String text;
    private int rating;
}
