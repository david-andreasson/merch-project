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
              "rating": 1–5,
              "text": "…"
            }
            
            "name" är ett påhittat namn på en person.
            
            Använd detta väder för att påverka recensionens humör, finare väder ger bättre recension:
            %s
            """;

    // Sätt true för mock-läge, false för produktion
    private static boolean USE_MOCK = false;
    // Final togs bort för att kunna köra tester

//    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final WeatherService weatherService;
//    private final String openAiApiKey;
//    private final String openAiApiUrl;
    private final String oneMinAiApiKey;
    private final String oneMinAiApiUrl;

    public AiReviewService(
            WeatherService weatherService,
//            RestTemplate restTemplate,
            ObjectMapper objectMapper,
//            @Value("${OPENAI_API_KEY}") String openAiApiKey,
//            @Value("${OPENAI_API_URL}") String openAiApiUrl,
            @Value("${ONEMINAI_API_KEY}") String oneMinAiApiKey,
            @Value("${ONEMINAI_API_URL}") String oneMinAiApiUrl
    ) {
        this.weatherService = weatherService;
//        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
//        this.openAiApiKey = openAiApiKey;
//        this.openAiApiUrl = openAiApiUrl;
        this.oneMinAiApiKey = oneMinAiApiKey;
        this.oneMinAiApiUrl = oneMinAiApiUrl;
    }

    // Genererar en Review för produkten, antingen mockad eller via riktigt ai-anrop (när USE_MOCK=false och requestAiReview är implementerad).
    public Review generateReview(Product product) throws IOException, InterruptedException {

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
                      "rating": 5,
                      "text": "Detta är en mockad recension."
                    }
                    """;
        } else {
            jsonResponse = requestAiReview(prompt);
        }

        //If ai responds with more than a json-object, remove everything before and after brackets
        if (!(jsonResponse.startsWith("{") && jsonResponse.endsWith("}"))) {
            int i = jsonResponse.indexOf("{");
            int j = jsonResponse.indexOf("}");
            jsonResponse = jsonResponse.substring(i, j+1);
        }

        // Gör om JSON till ReviewDto
        System.out.println(jsonResponse);
        ReviewDto dto = objectMapper.readValue(jsonResponse, ReviewDto.class);

        // Konvertera ReviewDto till Review-entitet
        Review review = new Review(dto.name(), dto.text(), dto.rating(), true);
        review.setDate(getRandomDate());
        review.setProduct(product);

        return review;
    }

    //Return random date within the last two months
    private LocalDate getRandomDate() {
        Random rand = new Random();
        LocalDate today = LocalDate.now();
        LocalDate twoMonthsAgo = LocalDate.now().minusMonths(2);

        long randomDate = rand.nextLong(twoMonthsAgo.toEpochDay(), today.toEpochDay());
        return LocalDate.ofEpochDay(randomDate);
    }

//    private String requestAiReview(String prompt) throws JsonProcessingException {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(openAiApiKey);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        Map<String, Object> body = Map.of(
//                "model", "gpt-4",
//                "messages", List.of(
//                        Map.of("role", "system", "content", "Du är en hjälpsam AI som skriver produktrecensioner."),
//                        Map.of("role", "user", "content", prompt)
//                )
//        );
//
//        //Skicka POST och hämta rå JSON
//        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
//        String raw = restTemplate.postForObject(openAiApiUrl, req, String.class);
//
//        //Extrahera bara innehållet från svaret
//        JsonNode root = objectMapper.readTree(raw);
//        return root
//                .path("choices").get(0)
//                .path("message")
//                .path("content")
//                .asText();
//    }

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

        //Skicka POST och hämta rå JSON
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

    // Intern DTO-klass för JSON-parsning
    private static record ReviewDto(
            String name,
            int rating,
            String text
    ) {
    }
}
