package com.jin12.reviews_api.service;

import com.jin12.reviews_api.dto.ReviewRespons;
import com.jin12.reviews_api.dto.ReviewStatsResponse;
import com.jin12.reviews_api.dto.ReviewsRespons;
import com.jin12.reviews_api.exception.ProductNotFoundException;
import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.model.Review;
import com.jin12.reviews_api.repository.ProductRepository;
import com.jin12.reviews_api.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * ReviewService handles operations related to product reviews.
 * Key responsibilities:
 * - Add a review for a product
 * - Delete reviews
 * - Fetch recent reviews (with AI fallback if below threshold)
 * - Compute review statistics for a product
 * - Package reviews and stats into a single response
 */
@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

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

    /**
     * Adds a new review for the given productId.
     * Fetches the Product entity, sets the review's product and date, then saves it.
     *
     * @param productId the full product ID (including user prefix)
     * @param review    the Review entity to add
     * @return the saved Review entity
     * @throws RuntimeException if the product is not found
     */
    public Review addReview(String productId, Review review) {
        log.debug("addReview – start, productId={}, reviewer={}", productId, review.getName());
        // Fetch product by ID, throw if missing
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("addReview – produkt saknas productId={}", productId);
                    return new RuntimeException("Produkt finns inte");
                });

        // Associate review with product and set current date
        review.setProduct(product);
        review.setDate(LocalDate.now());
        Review saved = reviewRepository.save(review);
        log.info("addReview – sparad recension id={} för productId={}", saved.getId(), productId);
        return saved;
    }

    /**
     * Deletes a single review by its ID.
     *
     * @param reviewId the ID of the review to delete
     */
    public void deleteReview(Long reviewId) {
        log.info("deleteReview – försök radera reviewId={}", reviewId);
        reviewRepository.deleteById(reviewId);
        log.info("deleteReview – recension raderad reviewId={}", reviewId);
    }

    /**
     * Deletes all reviews associated with the given productId.
     *
     * @param productId the full product ID whose reviews should be deleted
     */
    public void deleteReviewsByProductId(String productId) {
        log.info("deleteReviewsByProductId – försök radera recensioner för productId={}", productId);
        reviewRepository.deleteByProductId(productId);
        log.info("deleteReviewsByProductId – raderade recensioner för productId={}", productId);
    }

    /**
     * Fetches recent reviews for a product. Considers reviews from the past two months.
     * If fewer than MIN_REVIEWS are found, generates additional AI reviews up to MIN_REVIEWS.
     * Limits total reviews to MAX_REVIEWS before AI generation.
     *
     * @param productId the full product ID
     * @return list of Review entities combining real and any AI-generated reviews
     * @throws ProductNotFoundException if the product is not found
     */
    public List<Review> getRecentReviews(String productId) throws IllegalArgumentException {
        log.debug("getRecentReviews – start för productId={}", productId);
        // Fetch product, or throw if missing
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("getRecentReviews – produkt saknas productId={}", productId);
                    return new ProductNotFoundException("Product does not exist");
                });

        // Date threshold two months ago
        LocalDate fromDate = LocalDate.now().minusMonths(2);

        // Fetch all reviews for this product since fromDate
        List<Review> allRecentReviews = reviewRepository.findByProductAndDateAfter(product, fromDate);
        log.debug("getRecentReviews – hittade {} recensioner", allRecentReviews.size());

        // Limit to MAX_REVIEWS if more found
        if (allRecentReviews.size() > MAX_REVIEWS) {
            allRecentReviews = allRecentReviews.subList(0, MAX_REVIEWS);
        }

        // If fewer than MIN_REVIEWS, generate missing reviews via AI
        int missing = MIN_REVIEWS - allRecentReviews.size();
        if (missing > 0) {
            log.info("getRecentReviews – genererar {} AI-recension(er) för productId={}", missing, productId);
        }
        try {
            for (int i = 0; i < missing; i++) {
                Review aiReview = aiReviewService.generateReview(product);
                reviewRepository.save(aiReview);
                allRecentReviews.add(aiReview);
                log.debug("getRecentReviews – AI-recension genererad id={}", aiReview.getId());
            }
        } catch (Exception e) {
            log.error("getRecentReviews – fel vid AI-generering för productId={}", productId, e);
            e.printStackTrace();
        }

        // Return combined list of reviews
        log.debug("getRecentReviews – totala recensioner returnerade={}", allRecentReviews.size());
        return allRecentReviews;
    }

    /**
     * Computes review statistics for a product:
     * - Fetches reviews from the last two months; if fewer than MIN_REVIEWS, fetches top 10 by date.
     * - Calculates average rating and retrieves the date of the most recent review.
     *
     * @param productId the full product ID
     * @return a ReviewStatsResponse DTO containing statistics
     * @throws RuntimeException if the product is not found
     */
    public ReviewStatsResponse getProductStats(String productId) {
        log.debug("getProductStats – start för productId={}", productId);
        // Fetch product, or throw if missing
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("getProductStats – produkt saknas productId={}", productId);
                    return new RuntimeException("Produkt finns inte");
                });

        // Date threshold two months ago
        LocalDate twoMonthsAgo = LocalDate.now().minusMonths(2);

        // Fetch reviews from recent two months
        List<Review> recentReviews = reviewRepository.findByProductAndDateAfter(product, twoMonthsAgo);
        log.debug("getProductStats – hittade {} recensioner senaste två månaderna", recentReviews.size());

        // If fewer than MIN_REVIEWS, fetch top 10 most recent reviews regardless of date
        if (recentReviews.size() < MIN_REVIEWS) {
            log.info("getProductStats – färre än {} recensioner, hämtar topp 10 senaste", MIN_REVIEWS);
            recentReviews = reviewRepository.findTop10ByProductOrderByDateDesc(product);
        }

        // Calculate average rating
        double average = recentReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // Get date of the latest review (first in list if sorted by date desc)
        String lastReviewDate = recentReviews.isEmpty() ? null : recentReviews.get(0).getDate().toString();

        // Build and return DTO
        ReviewStatsResponse response = new ReviewStatsResponse();
        response.setProductId(product.getProductId());
        response.setProductName(product.getProductName());
        response.setCurrentAverage(average);
        response.setTotalReviews(recentReviews.size());
        response.setLastReviewDate(lastReviewDate);

        log.debug("getProductStats – färdigt för productId={}, totalReviews={}, average={}",
                productId, response.getTotalReviews(), response.getCurrentAverage());
        return response;
    }

    /**
     * Retrieves both reviews and statistics for a product.
     * Uses getRecentReviews() and getProductStats() to populate a combined DTO.
     *
     * @param productId the full product ID
     * @return a ReviewsRespons DTO containing both stats and review list
     */
    public ReviewsRespons getReviewsForProduct(String productId) {
        log.debug("getReviewsForProduct – start för productId={}", productId);
        // Fetch recent reviews
        List<Review> entities = getRecentReviews(productId);
        // Map Review entities to DTOs
        List<ReviewRespons> dtos = entities.stream()
                .map(r -> ReviewRespons.builder()
                        .date(r.getDate())
                        .name(r.getName())
                        .rating(r.getRating())
                        .text(r.getReviewText())
                        .build())
                .toList();
        // Fetch review statistics
        ReviewStatsResponse stats = getProductStats(productId);
        // Package into a combined response DTO
        ReviewsRespons result = ReviewsRespons.builder()
                .productId(productId)
                .stats(stats)
                .reviews(dtos)
                .build();
        log.info("getReviewsForProduct – returnerar ReviewsRespons för productId={}, reviewsCount={}",
                productId, dtos.size());
        return result;
    }
}