package com.jin12.reviews_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AiReviewServiceTest {

    private AiReviewService aiReviewService;

    @BeforeEach
    void setUp() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        // Sätt mockade värden direkt i testet
        String openAiApiKey = "mock-api-key";  // Mockat värde
        String openAiApiUrl = "https://mockapi.openai.com";  // Mockat värde

        aiReviewService = new AiReviewService(
                restTemplate,
                objectMapper,
                openAiApiKey,
                openAiApiUrl
        );

        // Aktivera mockläge genom att sätta USE_MOCK till true via reflektion
        Field useMockField = AiReviewService.class.getDeclaredField("USE_MOCK");
        useMockField.setAccessible(true); // Gör fältet tillgängligt

        // Sätt USE_MOCK till true
        useMockField.set(null, true);
    }

    @Test
    void testGenerateReview_Mock() throws Exception {
        // Arrange
        Product product = new Product();
        product.setProductId("abc123");
        product.setProductName("Testprodukt");
        product.setCategory("Elektronik");
        product.setTags("smart, teknologi");  // tags som en sträng

        // Act
        Review review = aiReviewService.generateReview(product);

        // Assert
        assertThat(review).isNotNull();
        assertThat(review.getName()).isEqualTo("TestUser");
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getReviewText()).contains("mockad recension");
        assertThat(review.isGeneratedByAI()).isTrue();
        assertThat(review.getProduct()).isEqualTo(product);
        assertThat(review.getDate()).isEqualTo(LocalDate.of(2025, 4, 1));
    }
}