package com.jin12.reviews_api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiReviewServiceTest {

    private WeatherService weatherService;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private AiReviewService aiReviewService;

    @BeforeEach
    void setUp() {
        weatherService = mock(WeatherService.class);
        restTemplate = mock(RestTemplate.class);
        objectMapper = new ObjectMapper();
        aiReviewService = new AiReviewService(
                weatherService,
                restTemplate,
                objectMapper,
                "fake-api-key",
                "https://fake.api.url"
        );

        // Aktivera mockläge så vi inte behöver OpenAI-anrop
        try {
            var field = AiReviewService.class.getDeclaredField("USE_MOCK");
            field.setAccessible(true);
            field.set(null, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void generateReview_returnsMockedReview() throws JsonProcessingException {
        // Arrange
        when(weatherService.getWeather()).thenReturn("Soligt");

        Product product = new Product();
        product.setProductName("Testprodukt");
        product.setCategory("Elektronik");
        product.setTags("test, elektronik");

        // Act
        Review review = aiReviewService.generateReview(product);

        // Assert
        assertNotNull(review);
        assertEquals("TestUser", review.getName());
        assertEquals("Detta är en mockad recension.", review.getReviewText());
        assertEquals(5, review.getRating());
        assertTrue(review.isGeneratedByAI());
        assertEquals(product, review.getProduct());
    }
}
