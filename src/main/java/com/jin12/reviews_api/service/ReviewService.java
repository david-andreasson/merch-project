package com.jin12.reviews_api.service;

import com.jin12.reviews_api.dto.ReviewRespons;
import com.jin12.reviews_api.dto.ReviewStatsResponse;
import com.jin12.reviews_api.dto.ReviewsRespons;
import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.model.Review;
import com.jin12.reviews_api.repository.ProductRepository;
import com.jin12.reviews_api.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final AiReviewService aiReviewService;

    private static final int MIN_REVIEWS = 5;
    private static final int MAX_REVIEWS = 10;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository, AiReviewService aiReviewService) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.aiReviewService = aiReviewService;
    }

    public Review addReview(String productId, Review review) {
        // Hämta produkt baserat på productId
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produkt finns inte"));

        // Sätt produkt för recensionen
        review.setProduct(product);
        review.setDate(LocalDate.now());  // Sätt dagens datum som skapelsedatum
        return reviewRepository.save(review);
    }

    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);  // Ta bort recensionen från databasen
    }

    public void deleteReviewsByProductId(String productId) {
        reviewRepository.deleteByProductId(productId);
    }

    public List<Review> getRecentReviews(String productId) throws IllegalArgumentException {
        // Hämta produkt baserat på productId
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produkt finns inte"));

        // Datumgräns för de senaste 2 månaderna
        LocalDate fromDate = LocalDate.now().minusMonths(2);

        // Hämtar alla recensioner för de senaste 2 månaderna
        List<Review> allRecentReviews =
                reviewRepository.findByProductAndDateAfter(product, fromDate);

        //Limit number of reviews to MAX_REVIEWS
        if (allRecentReviews.size() > MAX_REVIEWS) {
            allRecentReviews = allRecentReviews.subList(0, MAX_REVIEWS);
        }

        // Om vi har mindre än MIN_REVIEWS, ai-generera de som saknas
        int missing = MIN_REVIEWS - allRecentReviews.size();
        try {
            for (int i = 0; i < missing; i++) {
                Review aiReview = aiReviewService.generateReview(product);
                reviewRepository.save(aiReview);
                allRecentReviews.add(aiReview);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Returnerar både gamla och nygenererade recensioner
        return allRecentReviews;
    }

    // Hämta statistik + senaste recensioner
    public ReviewStatsResponse getProductStats(String productId) {
        // Hämta produkten
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produkt finns inte"));

        // Definiera två månader bakåt
        LocalDate twoMonthsAgo = LocalDate.now().minusMonths(2);

        // Hämta recensioner från senaste två månaderna
        List<Review> recentReviews = reviewRepository.findByProductAndDateAfter(product, twoMonthsAgo);

        // Om färre än 5 recensioner → hämta senaste 10 recensionerna oavsett datum
        if (recentReviews.size() < 5) {
            recentReviews = reviewRepository.findTop10ByProductOrderByDateDesc(product);
        }

        // Räkna ut medelbetyg (currentAverage)
        double average = recentReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // Hämta datum för senaste recension (lastReviewDate)
        String lastReviewDate = recentReviews.isEmpty() ? null : recentReviews.get(0).getDate().toString();

        // Bygg svaret (DTO)
        ReviewStatsResponse response = new ReviewStatsResponse();
        response.setProductId(product.getProductId());
        response.setProductName(product.getProductName());
        response.setCurrentAverage(average);
        response.setTotalReviews(recentReviews.size());
        response.setLastReviewDate(lastReviewDate);

        return response;
    }

    // Wrapper som returnerar recensioner och statistik
    public ReviewsRespons getReviewsForProduct(String productId) {
        // Hämta recensioner med befintlig metod
        List<Review> entities = getRecentReviews(productId);
        // Mappa till DTO
        List<ReviewRespons> dtos = entities.stream()
                .map(r -> ReviewRespons.builder()
                        .date(r.getDate())
                        .name(r.getName())
                        .rating(r.getRating())
                        .text(r.getReviewText())
                        .build())
                .toList();
        // Hämta statistik med befintlig metod
        ReviewStatsResponse stats = getProductStats(productId);
        // Paketera allt i ReviewsRespons
        return ReviewsRespons.builder()
                .productId(productId)
                .stats(stats)
                .reviews(dtos)
                .build();
    }
}

