package com.jin12.reviews_api.service;

import org.springframework.stereotype.Service;
import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.model.Review;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;

@Service
public class AiReviewService {

    // Hårdkodad prompt-mall som genererar en recension
    private static final String PROMPT_TEMPLATE = """
            Du är en kund som recenserar produkten:
            - Namn: %s
            - Kategori: %s
            - Taggar: %s
            
            Skriv en recension som ett JSON-objekt:
            {
              "name": "…",
              "date": "YYYY-MM-DD",
              "rating": 1–5,
              "text": "…"
            }
            """;

    // Sätt true för mock-läge, false för produktion
    private static final boolean USE_MOCK = true;

    private final ObjectMapper objectMapper = new ObjectMapper();


    // Genererar en Review för produkten, antingen mockad eller via riktigt ai-anrop (när USE_MOCK=false och requestAiReview är implementerad).
    public Review generateReview(Product product) throws Exception {

        //Bygg prompt-strängen
        String prompt = String.format(
                PROMPT_TEMPLATE,
                product.getProductName(),
                product.getCategory(),
                product.getTags()
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
        ReviewDto dto = objectMapper.readValue(jsonResponse, ReviewDto.class);

        // Konvertera ReviewDto till Review-entitet
        Review review = new Review(dto.name(), dto.text(), dto.rating(), true);
        review.setDate(LocalDate.parse(dto.date()));
        review.setProduct(product);

        return review;
    }

    // TODO: Byt ut mot riktigt HTTP-anrop mot OpenAI eller vilken tjänst vi nu kommer få använda
    private String requestAiReview(String prompt) {
        throw new UnsupportedOperationException("Ser du den här raden så kör du USE_MOCK=false utan att ha implementerat det riktiga AI-anropet");
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
