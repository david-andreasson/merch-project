package com.jin12.reviews_api.service;

import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.model.Review;
import com.jin12.reviews_api.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public List<Review> getRecentReviews(Product product) {

        LocalDate fromDate = LocalDate.now().minusMonths(2);

        return reviewRepository.findByProductAndDateAfter(product, fromDate);
    }
}
