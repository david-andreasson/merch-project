package com.jin12.reviews_api.controller;

import com.jin12.reviews_api.dto.*;
import com.jin12.reviews_api.exception.*;
import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.model.Review;
import com.jin12.reviews_api.model.User;
import com.jin12.reviews_api.dto.ProductInfo;
import com.jin12.reviews_api.service.ApiKeyService;
import com.jin12.reviews_api.service.ProductService;
import com.jin12.reviews_api.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final ReviewService reviewService;
    private final ApiKeyService apiKeyService;

    @GetMapping("/{productId}")
    public ResponseEntity<ReviewsRespons> getReviews(
            @PathVariable String productId,
            @AuthenticationPrincipal User currentUser) {
        String fullProductId = currentUser.getId().toString() + productId;
        log.info("getReviews – productId={}, fullProductId={}, userId={}", productId, fullProductId, currentUser.getId());
        ReviewsRespons resp = reviewService.getReviewsForProduct(fullProductId);
        log.debug("getReviews – returning {} reviews for fullProductId={}", resp.getReviews().size(), fullProductId);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductRespons>> getAllProducts(
            @AuthenticationPrincipal User currentUser) {
        List<Product> products = productService.getProductsByUser(currentUser.getId());
        List<ProductRespons> productResponsList = new ArrayList<>();
        for (Product product : products) {
            //Remove userId from productId
            int userIdSize = currentUser.getId().toString().length();
            String productId = product.getProductId().substring(userIdSize);
            productResponsList.add(ProductRespons.builder()
                            .productId(productId)
                            .productName(product.getProductName())
                            .category(product.getCategory())
                            .tags(product.getTags())
                            .build());
        }
        return ResponseEntity.ok(productResponsList);
    }


    @PostMapping
    public ResponseEntity<Object> addProducts(@RequestBody ProductRequest productRequest,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        log.info("addProducts – mode={} by userId={}", productRequest.getMode(), user.getId());

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
                log.warn("addProducts – unknown mode={} by userId={}", productRequest.getMode(), user.getId());
                throw new BadRequestException("Invalid mode");
        }
        log.debug("addProducts – response status={}", respons.getStatusCode());
        return respons;
    }

    private ResponseEntity<Object> handleWithUrl(ProductRequest productRequest, User user) {
        log.info("handleWithUrl – productInfoUrl={} by userId={}", productRequest.getProductInfoUrl(), user.getId());
        if (productRequest.getProductId() == null) {
            log.warn("handleWithUrl – missing product URL for userId={}", user.getId());
            throw new BadRequestException("Missing product URL");
        }
        String apiKey;
        try {
            apiKey = apiKeyService.getDecryptedApiKey(user);
            if (apiKey == null || apiKey.isEmpty()) {
                log.warn("handleWithUrl – no API key configured for userId={}", user.getId());
                throw new ApiKeyException("User has no API key configured");
            }
        } catch (Exception e) {
            log.error("handleWithUrl – failed to decrypt API key for userId={}", user.getId(), e);
            throw new ApiKeyException("Failed to decrypt API key or no Api key exists", e);
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
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
                log.warn("handleWithUrl – empty body from external service for userId={}", user.getId());
                throw new BadRequestException("Url did not work correctly");
            }
            productRequest.setProductName(info.getProductName());
            productRequest.setCategory(info.getCategory());
            productRequest.setTags(info.getTags());

            return handleWithDetails(productRequest, user);
        }catch (RestClientException e) {
            log.error("handleWithUrl – error calling external service for userId={}", user.getId(), e);
            throw new ExternalServiceException("Error calling external product info service", e);
        }
    }

    private ResponseEntity<Object> handleCustomReview(ProductRequest productRequest, User user) {
        String productId = user.getId() + productRequest.getProductId();
        log.info("handleCustomReview – fullProductId={}, reviewer={}", productId, productRequest.getReview().getName());


            Product product = productService.getProductById(productId);
            if (product == null) {
                throw new ProductNotFoundException("Product does not exist");
            }
            ReviewRequest reviewRequest = productRequest.getReview();
            Review review = new Review(
                    reviewRequest.getName(),
                    reviewRequest.getText(),
                    reviewRequest.getRating(),
                    false);
            reviewService.addReview(productId, review);
            log.info("handleCustomReview – review added for fullProductId={}", productId);
            return ResponseEntity.status(HttpStatus.CREATED).body("Review added successfully");

    }

    private ResponseEntity<Object> handleWithDetails(ProductRequest productRequest, User user) {
        String productId = user.getId() + productRequest.getProductId();
        log.info("handleWithDetails – fullProductId={}", productId);
        Product product = null;
        try {
            product = productService.getProductById(productId);
        } catch (RuntimeException e) {
            //if catch then continue
        }

            if (product != null) {
                log.warn("handleWithDetails – product already exists fullProductId={}", productId);
                throw new ProductAlreadyExistsException("Product already exists");
            }

            log.debug("handleWithDetails – creating product fullProductId={}", productId);
            product = Product.builder()
                    .productId(productId)
                    .productName(productRequest.getProductName())
                    .category(productRequest.getCategory())
                    .tags(String.join(", ", productRequest.getTags()))
                    .user(user)
                    .build();
            productService.addProduct(product);
            log.info("handleWithDetails – product created fullProductId={}", productId);


        // Bygg productrespons med både produktinfo och reviews
        ProductRespons productRespons = ProductRespons.builder()
                .productId(productRequest.getProductId())
                .productName(product.getProductName())
                .category(product.getCategory())
                .tags(product.getTags())
                .build();
        log.debug("handleWithDetails – returning ProductRespons fullProductId={}", productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(productRespons);
    }

    private ResponseEntity<Object> handleProductOnly(ProductRequest productRequest, User user) {
        log.info("handleProductOnly – setting defaults for userId={}", user.getId());
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
        log.info("deleteProduct – productId={}, fullProductId={}, userId={}", productId, fullProductId, user.getId());

        try {
            // Ta bort relaterade recensioner först
            reviewService.deleteReviewsByProductId(fullProductId);
            // Ta sedan bort produkten
            productService.deleteProduct(fullProductId);
            log.info("deleteProduct – deleted product and reviews fullProductId={}", fullProductId);
            return ResponseEntity.ok("Product and related reviews deleted successfully");
        } catch (Exception e) {
            log.warn("deleteProduct – failed to delete fullProductId={}", fullProductId, e);
            throw new ProductNotFoundException("Product not found and not deleted");
        }
    }
}
