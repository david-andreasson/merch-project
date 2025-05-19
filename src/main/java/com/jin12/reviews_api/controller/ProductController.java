package com.jin12.reviews_api.controller;

import com.jin12.reviews_api.dto.*;
import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.model.Review;
import com.jin12.reviews_api.model.User;
import com.jin12.reviews_api.dto.ProductInfo;
import com.jin12.reviews_api.service.ApiKeyService;
import com.jin12.reviews_api.service.ProductService;
import com.jin12.reviews_api.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ReviewService reviewService;
    private final ApiKeyService apiKeyService;

    @GetMapping
    public ResponseEntity<Object> getReviews(@RequestBody ProductRequest productRequest,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        String productId = user.getId() + productRequest.getProductId();

        try {
            List<Review> reviews = reviewService.getRecentReviews(productId);
            List<ReviewRespons> reviewResponses = new ArrayList<>();

            double totalRating = 0;
            for (Review review : reviews) {
                reviewResponses.add(
                        ReviewRespons.builder()
                                .date(review.getDate())
                                .name(review.getName())
                                .rating(review.getRating())
                                .text(review.getReviewText())
                                .build());
                totalRating += review.getRating();
            }

            ReviewsRespons reviewsRespons = ReviewsRespons.builder()
                    .productId(productRequest.getProductId())
                    .stats(reviewService.getProductStats(productId))
                    .reviews(reviewResponses)
                    .build();

            return ResponseEntity.ok(reviewsRespons);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Product does not exist");
        }
    }

    @PostMapping
    public ResponseEntity<Object> addProducts(@RequestBody ProductRequest productRequest,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;

        ResponseEntity<Object> respons = null;
        switch (productRequest.getMode()) {
            case "productOnly":
                respons = handleProductOnly(productRequest, user);
                break;
            case "withUrl":
                respons = handleWithUrl(productRequest, user);
                break;
            case "withDetails":
                respons = handleWithDetails(productRequest, user);
                break;
            case "customReview":
                respons = handleCustomReview(productRequest, user);
                break;
            default:
//                respons = new Product();
        }

        return respons;
    }

    private ResponseEntity<Object> handleWithUrl(ProductRequest productRequest, User user) {
        if (productRequest.getProductId() == null) {
            return ResponseEntity.badRequest().body("Missing product URL");
        }
        String apiKey;
        try {
            apiKey = apiKeyService.getDecryptedApiKey(user);
            if (apiKey == null || apiKey.isEmpty()) {
                return ResponseEntity.badRequest().body("User has no API key configured");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to decrypt API key");
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        System.out.println(apiKey);
//        headers.setBearerAuth(apiKey);
        headers.set("X-API-KEY", apiKey);  // Anpassa header enligt API
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ProductInfo> response = restTemplate.exchange(
                    productRequest.getProductInfoUrl(),
                    HttpMethod.GET,
                    requestEntity,
                    ProductInfo.class
            );

            ProductInfo info = response.getBody();
            if (info == null) {
                return ResponseEntity.badRequest().body("URL did not work correctly");
            }
            productRequest.setProductName(info.getProductName());
            productRequest.setCategory(info.getCategory());
            productRequest.setTags(info.getTags());

            return handleWithDetails(productRequest, user);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Error calling external product info service: " + e.getMessage());
        }
    }

    private ResponseEntity<Object> handleCustomReview(ProductRequest productRequest, User user) {
        String productId = user.getId() + productRequest.getProductId();

        try {
            Product product = productService.getProductById(productId);
            ReviewRequest reviewRequest = productRequest.getReview();
            Review review = new Review(
                    reviewRequest.getName(),
                    reviewRequest.getText(),
                    reviewRequest.getRating(),
                    false);
            reviewService.addReview(productId, review);
            return ResponseEntity.status(HttpStatus.CREATED).body("Review added successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Product does not exist");
        }
    }

    private ResponseEntity<Object> handleWithDetails(ProductRequest productRequest, User user) {
        String productId = user.getId() + productRequest.getProductId();
        Product product;

        try {
            product = productService.getProductById(productId);
            return ResponseEntity.badRequest().body("Product already exists");
        } catch (Exception e) {
            product = Product.builder()
                    .productId(productId)
                    .productName(productRequest.getProductName())
                    .category(productRequest.getCategory())
                    .tags(String.join(", ", productRequest.getTags()))
                    .user(user)
                    .build();
            productService.addProduct(product);
        }

        // Bygg productrespons med både produktinfo och reviews
        ProductRespons productRespons = ProductRespons.builder()
                .productId(productRequest.getProductId())
                .productName(product.getProductName())
                .category(product.getCategory())
                .tags(product.getTags())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(productRespons);
    }

    private ResponseEntity<Object> handleProductOnly(ProductRequest productRequest, User user) {
        productRequest.setProductName("Whitesnake T-shirt");
        productRequest.setCategory("T-shirt");
        productRequest.setTags(List.of("hårdrock", "80-tal", "svart", "bomull"));
        return handleWithDetails(productRequest, user);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Object> deleteProduct(@PathVariable String productId,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        String fullProductId = user.getId() + productId;

        try {
            // Ta bort relaterade recensioner först
            reviewService.deleteReviewsByProductId(fullProductId);

            // Ta sedan bort produkten
            productService.deleteProduct(fullProductId);

            return ResponseEntity.ok("Product and related reviews deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Product with ID " + productId + " not found or could not be deleted");
        }
    }
}
