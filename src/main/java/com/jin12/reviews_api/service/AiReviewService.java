package com.jin12.reviews_api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.model.Review;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class AiReviewService {

//     Hårdkodad prompt-mall som genererar en recension
    private static final String PROMPT_TEMPLATE = """
            Du är en kund som recenserar produkten:
            - Namn: %s
            - Kategori: %s
            - Taggar: %s

            Skriv och svara endast med en recension som ett komplett JSON-objekt:
            {
              "name": "…",
              "date": "YYYY-MM-DD",
              "rating": 1–5,
              "text": "…"
            }

            "name" är ett påhittat namn på en person.
            "date" får bara vara dem senaste två månaderna.

            Använd detta väder för att påverka recensionens humör, finare väder ger sämre recension:
            %s
            """;

    // Sätt true för mock-läge, false för produktion
    private static boolean USE_MOCK = false;
    // Final togs bort för att kunna köra tester


    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final WeatherService weatherService;
    private final String openAiApiKey;
    private final String openAiApiUrl;

    public AiReviewService(
            WeatherService weatherService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${OPENAI_API_KEY}") String openAiApiKey,
            @Value("${OPENAI_API_URL}") String openAiApiUrl
    ) {
        this.weatherService = weatherService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.openAiApiKey = openAiApiKey;
        this.openAiApiUrl = openAiApiUrl;
    }


    // Genererar en Review för produkten, antingen mockad eller via riktigt ai-anrop (när USE_MOCK=false och requestAiReview är implementerad).
    public Review generateReview(Product product) throws JsonProcessingException {

        //Bygg prompt-strängen
        String prompt = String.format(
                PROMPT_TEMPLATE,
                product.getProductName(),
                product.getCategory(),
                product.getTags(),
                weatherService.getWeather()
        );

        // Hämta JSON-respons
        String jsonResponse;
        if (USE_MOCK) {
            jsonResponse = """
                    {
                      "name": "TestUser",
                      "date": "2025-04-01",
                      "rating": 5,
                      "text": "Detta är en mockad recension."
                    }
                    """;
        } else {
            jsonResponse = requestAiReview(prompt);
        }

        // Gör om JSON till ReviewDto
        System.out.println(jsonResponse);
        ReviewDto dto = objectMapper.readValue(jsonResponse, ReviewDto.class);

        // Konvertera ReviewDto till Review-entitet
        Review review = new Review(dto.name(), dto.text(), dto.rating(), true);
        review.setDate(LocalDate.parse(dto.date()));
        review.setProduct(product);

        return review;
    }

    private String requestAiReview(String prompt) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openAiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "model", "gpt-4",
                "messages", List.of(
                        Map.of("role", "system", "content", "Du är en hjälpsam AI som skriver produktrecensioner."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        //Skicka POST och hämta rå JSON
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
        String raw = restTemplate.postForObject(openAiApiUrl, req, String.class);

        //Extrahera bara innehållet från svaret
        JsonNode root = objectMapper.readTree(raw);
        return root
                .path("choices").get(0)
                .path("message")
                .path("content")
                .asText();
    }

    // Intern DTO-klass för JSON-parsning
    private static record ReviewDto(
            String name,
            String date,
            int rating,
            String text
    ) {
    }
}
