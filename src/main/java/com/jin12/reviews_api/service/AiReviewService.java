package com.jin12.reviews_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.model.Review;
import io.swagger.v3.core.util.Json;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Map;
import java.util.Random;

/**
 * Service responsible for generating reviews using AI or mock data.
 * It builds a prompt based on product details and weather, then parses AI responses.
 */
@Service
public class AiReviewService {
    /**
     * Template for generating a prompt to request a product review from the AI.
     * Inserts product name, category, tags, and weather into the JSON prompt.
     */
    private static final String PROMPT_TEMPLATE = """
            Du är en kund som recenserar produkten:
            - Namn: %s
            - Kategori: %s
            - Taggar: %s
            
            Skriv och svara endast med en recension som ett komplett JSON-objekt:
            {
              "name": "…",
              "rating": 1–5,
              "text": "…"
            }
            
            "name" är ett påhittat namn på en person.
            
            Använd detta väder för att påverka recensionens humör, finare väder ger bättre recension:
            %s
            """;

    /**
     * If true, the service returns a mock review without calling the AI API.
     * Set to false to enable real AI API calls.
     */
    private static boolean USE_MOCK = false;

    private final ObjectMapper objectMapper;
    private final WeatherService weatherService;
    private final String oneMinAiApiKey;
    private final String oneMinAiApiUrl;

    /**
     * Constructs the AiReviewService with dependencies and configuration values.
     *
     * @param weatherService   service used to retrieve current weather data
     * @param objectMapper     JSON object mapper for parsing AI responses
     * @param oneMinAiApiKey   API key for the one-minute AI service
     * @param oneMinAiApiUrl   URL endpoint for the one-minute AI service
     */
    public AiReviewService(
            WeatherService weatherService,
            ObjectMapper objectMapper,
            @Value("${ONEMINAI_API_KEY}") String oneMinAiApiKey,
            @Value("${ONEMINAI_API_URL}") String oneMinAiApiUrl
    ) {
        this.weatherService = weatherService;
        this.objectMapper = objectMapper;
        this.oneMinAiApiKey = oneMinAiApiKey;
        this.oneMinAiApiUrl = oneMinAiApiUrl;
    }

    /**
     * Generates a Review entity for the given product.
     * If USE_MOCK is true, returns a hardcoded review. Otherwise, builds a prompt and calls the AI API.
     * Extracts JSON content from the AI response, converts it to a ReviewDto, then to a Review entity.
     *
     * @param product the product for which to generate a review
     * @return the generated Review entity, with a random date within the last two months
     * @throws IOException          if JSON parsing fails
     * @throws InterruptedException if the HTTP request to the AI service is interrupted
     */
    public Review generateReview(Product product) throws IOException, InterruptedException {

        // Build the prompt string
        String prompt = String.format(
                PROMPT_TEMPLATE,
                product.getProductName(),
                product.getCategory(),
                product.getTags(),
                weatherService.getWeather()
        );

        // Retrieve JSON response (mock or real)
        String jsonResponse;
        if (USE_MOCK) {
            jsonResponse = """
                    {
                      "name": "TestUser",
                      "rating": 5,
                      "text": "Detta är en mockad recension."
                    }
                    """;
        } else {
            jsonResponse = requestAiReview(prompt);
        }

        // If AI response contains extra text, strip everything before and after JSON brackets
        if (!(jsonResponse.startsWith("{") && jsonResponse.endsWith("}"))) {
            int i = jsonResponse.indexOf("{");
            int j = jsonResponse.indexOf("}");
            jsonResponse = jsonResponse.substring(i, j + 1);
        }

        // Convert JSON to ReviewDto
        ReviewDto dto = objectMapper.readValue(jsonResponse, ReviewDto.class);

        // Convert ReviewDto to Review entity
        Review review = new Review(dto.name(), dto.text(), dto.rating(), true);
        review.setDate(getRandomDate());
        review.setProduct(product);

        return review;
    }

    /**
     * Generates a random date within the last two months from today.
     *
     * @return a random LocalDate between two months ago and today
     */
    private LocalDate getRandomDate() {
        Random rand = new Random();
        LocalDate today = LocalDate.now();
        LocalDate twoMonthsAgo = LocalDate.now().minusMonths(2);

        long randomDate = rand.nextLong(twoMonthsAgo.toEpochDay(), today.toEpochDay());
        return LocalDate.ofEpochDay(randomDate);
    }

    /**
     * Sends a POST request to the AI service with the given prompt and returns the raw JSON response.
     *
     * @param prompt the prompt string to send to the AI API
     * @return the raw response body as a JSON string
     * @throws IOException          if sending or receiving the HTTP request fails
     * @throws InterruptedException if the HTTP request is interrupted
     */
    private String requestAiReview(String prompt) throws IOException, InterruptedException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(oneMinAiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "type", "CHAT_WITH_AI",
                "model", "gpt-4o-mini",
                "promptObject", Map.of(
                        "prompt", prompt,
                        "temperature", 0.7,
                        "max_tokens", 100,
                        "top_p", 0.9
                )
        );

        // Send POST request and retrieve raw JSON string
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(oneMinAiApiUrl))
                    .header("Content-Type", "application/json")
                    .header("API-KEY", oneMinAiApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(Json.pretty(body)))
                    .build();

            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }
    }

    /**
     * Internal DTO class used for parsing AI JSON responses into Java objects.
     *
     * @param name   the name of the reviewer
     * @param rating the rating given by the reviewer (1-5)
     * @param text   the review text content
     */
    private static record ReviewDto(
            String name,
            int rating,
            String text
    ) {
    }
}