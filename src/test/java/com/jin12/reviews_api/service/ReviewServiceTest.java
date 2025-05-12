package com.jin12.reviews_api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jin12.reviews_api.dto.ReviewStatsResponse;
import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.model.Review;
import com.jin12.reviews_api.repository.ProductRepository;
import com.jin12.reviews_api.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    private ReviewRepository reviewRepository;
    private ProductRepository productRepository;
    private AiReviewService aiReviewService;
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewRepository = mock(ReviewRepository.class);
        productRepository = mock(ProductRepository.class);
        aiReviewService = mock(AiReviewService.class);
        reviewService = new ReviewService(reviewRepository, productRepository, aiReviewService);
    }

    @Test
    void testAddReviewSuccess() {
        String productId = "prod1";
        Product product = new Product();
        Review review = new Review();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        Review saved = reviewService.addReview(productId, review);

        assertEquals(product, saved.getProduct());
        assertNotNull(saved.getDate());
        verify(reviewRepository).save(saved);
    }

    @Test
    void testAddReviewProductNotFound() {
        when(productRepository.findById("badId")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                reviewService.addReview("badId", new Review()));

        assertTrue(ex.getMessage().contains("Produkt finns inte"));
    }

    @Test
    void testDeleteReview() {
        reviewService.deleteReview(42L);
        verify(reviewRepository).deleteById(42L);
    }

    @Test
    void testGetRecentReviewsEnough() throws JsonProcessingException {
        Product product = new Product();
        product.setProductId("prod");
        List<Review> recentReviews = createReviews(5);

        when(productRepository.findById("prod")).thenReturn(Optional.of(product));
        when(reviewRepository.findByProductAndDateAfter(eq(product), any()))
                .thenReturn(new ArrayList<>(recentReviews));

        List<Review> result = reviewService.getRecentReviews("prod");

        assertEquals(5, result.size());
        verify(aiReviewService, never()).generateReview(any());
    }

    @Test
    void testGetRecentReviewsWithAiGenerated() throws Exception {
        Product product = new Product();
        product.setProductId("prod");
        List<Review> existingReviews = createReviews(2);
        Review aiReview = new Review();
        aiReview.setRating(4);
        aiReview.setDate(LocalDate.now());

        when(productRepository.findById("prod")).thenReturn(Optional.of(product));
        when(reviewRepository.findByProductAndDateAfter(eq(product), any()))
                .thenReturn(new ArrayList<>(existingReviews));
        when(aiReviewService.generateReview(product)).thenReturn(aiReview);
        when(reviewRepository.save(any())).thenReturn(aiReview);

        List<Review> result = reviewService.getRecentReviews("prod");

        assertEquals(5, result.size());
        verify(aiReviewService, times(3)).generateReview(product);
    }

    @Test
    void testGetRecentReviewsProductNotFound() {
        when(productRepository.findById("bad")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                reviewService.getRecentReviews("bad"));
    }

    @Test
    void testGetProductStatsEnoughReviews() {
        Product product = new Product();
        product.setProductId("p1");
        product.setProductName("TestProduct");
        List<Review> reviews = createReviews(5);
        reviews.get(0).setDate(LocalDate.now()); // Ensuring latest

        when(productRepository.findById("p1")).thenReturn(Optional.of(product));
        when(reviewRepository.findByProductAndDateAfter(eq(product), any())).thenReturn(reviews);

        ReviewStatsResponse response = reviewService.getProductStats("p1");

        assertEquals("p1", response.getProductId());
        assertEquals("TestProduct", response.getProductName());
        assertEquals(5, response.getTotalReviews());
        assertNotNull(response.getLastReviewDate());
    }

    @Test
    void testGetProductStatsFallbackToTop10() {
        Product product = new Product();
        product.setProductId("p2");
        product.setProductName("FallbackProduct");

        when(productRepository.findById("p2")).thenReturn(Optional.of(product));
        when(reviewRepository.findByProductAndDateAfter(eq(product), any()))
                .thenReturn(Collections.emptyList());

        List<Review> fallback = createReviews(3);
        fallback.get(0).setDate(LocalDate.now());
        when(reviewRepository.findTop10ByProductOrderByDateDesc(product)).thenReturn(fallback);

        ReviewStatsResponse response = reviewService.getProductStats("p2");

        assertEquals("FallbackProduct", response.getProductName());
        assertEquals(3, response.getTotalReviews());
    }

    @Test
    void testGetProductStatsProductNotFound() {
        when(productRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                reviewService.getProductStats("missing"));
    }

    @Test
    void testDeleteReviewsByProductId() {
        String productId = "some-product-id";
        reviewService.deleteReviewsByProductId(productId);
        verify(reviewRepository).deleteByProductId(productId);
    }

    // Utility method
    private List<Review> createReviews(int count) {
        List<Review> reviews = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Review r = new Review();
            r.setRating(4);
            r.setDate(LocalDate.now().minusDays(i));
            reviews.add(r);
        }
        return reviews;
    }
}
