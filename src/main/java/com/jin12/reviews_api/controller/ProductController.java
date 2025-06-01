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

/**
 * REST controller for managing products and reviews.
 * Provides endpoints to retrieve, add, and delete products/reviews.
 */
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final ReviewService reviewService;
    private final ApiKeyService apiKeyService;

    /**
     * Retrieves all reviews for a given product.
     *
     * @param productId   the client-visible product ID (without user prefix)
     * @param currentUser the authenticated user
     * @return a ResponseEntity containing a ReviewsRespons object with review data
     */
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

    /**
     * Retrieves a list of all products for the authenticated user.
     *
     * @param currentUser the authenticated user
     * @return a ResponseEntity containing a list of ProductRespons objects
     */
    @GetMapping("/all")
    public ResponseEntity<List<ProductRespons>> getAllProducts(
            @AuthenticationPrincipal User currentUser) {
        List<Product> products = productService.getProductsByUser(currentUser.getId());
        List<ProductRespons> productResponsList = new ArrayList<>();
        for (Product product : products) {
            // Remove userId prefix from stored productId
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

    /**
     * Adds a product or review based on the provided mode in the request.
     * Supported modes: "productOnly", "withUrl", "withDetails", "customReview".
     *
     * @param productRequest the request body containing product/review details
     * @param userDetails    the authenticated user's details
     * @return a ResponseEntity with operation-specific response
     * @throws BadRequestException         if the mode is invalid
     * @throws ApiKeyException             if API key decryption fails or is missing
     * @throws ExternalServiceException    if external service call fails
     * @throws ProductAlreadyExistsException if attempting to create a product that already exists
     * @throws ProductNotFoundException    if a product is not found when adding a custom review
     */
    @PostMapping
    public ResponseEntity<Object> addProducts(@RequestBody ProductRequest productRequest,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        log.info("addProducts – mode={} by userId={}", productRequest.getMode(), user.getId());

        ResponseEntity<Object> respons;
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

    /**
     * Handles adding a product by fetching details from an external URL.
     * Validates the user's API key before calling the external service.
     *
     * @param productRequest the request containing the productInfoUrl and other fields
     * @param user           the authenticated user
     * @return a ResponseEntity with created product details
     * @throws BadRequestException       if URL is missing or the external response body is empty
     * @throws ApiKeyException           if API key decryption fails or no key exists
     * @throws ExternalServiceException  if an error occurs during the external service call
     */
    private ResponseEntity<Object> handleWithUrl(ProductRequest productRequest, User user) {
        // Flow: 1) decrypt user's API key, 2) call external service, 3) map response to ProductInfo, 4) delegate to handleWithDetails
        log.info("handleWithUrl – productInfoUrl={} by userId={}", productRequest.getProductInfoUrl(), user.getId());
        if (productRequest.getProductId() == null) {
            log.warn("handleWithUrl – missing product URL for userId={}", user.getId());
            throw new BadRequestException("Missing product URL");
        }

        String apiKey;
        try {
            // Decrypt the stored API key for the user
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
        headers.set("X-API-KEY", apiKey);  // Set header according to API requirements
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
            // Set product details from external response
            productRequest.setProductName(info.getProductName());
            productRequest.setCategory(info.getCategory());
            productRequest.setTags(info.getTags());

            // Delegate to handleWithDetails to save the product
            return handleWithDetails(productRequest, user);
        } catch (RestClientException e) {
            log.error("handleWithUrl – error calling external service for userId={}", user.getId(), e);
            throw new ExternalServiceException("Error calling external product info service", e);
        }
    }

    /**
     * Handles adding a custom review for an existing product.
     *
     * @param productRequest the request containing review details
     * @param user           the authenticated user
     * @return a ResponseEntity with a success message and HTTP 201 status
     * @throws ProductNotFoundException if the product does not exist
     */
    private ResponseEntity<Object> handleCustomReview(ProductRequest productRequest, User user) {
        // fullProductId uses userId prefix to avoid ID collisions between users
        String productId = user.getId() + productRequest.getProductId();
        log.info("handleCustomReview – fullProductId={}, reviewer={}", productId, productRequest.getReview().getName());

        Product product = productService.getProductById(productId);
        if (product == null) {
            throw new ProductNotFoundException("Product does not exist");
        }
        // Create and save the new review
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

    /**
     * Handles creating a product with provided details. Throws exception if the product already exists.
     *
     * @param productRequest the request containing product details
     * @param user           the authenticated user
     * @return a ResponseEntity with created product data
     * @throws ProductAlreadyExistsException if a product with the same ID already exists
     */
    private ResponseEntity<Object> handleWithDetails(ProductRequest productRequest, User user) {
        // fullProductId uses userId prefix to avoid ID collisions between users
        String productId = user.getId() + productRequest.getProductId();
        log.info("handleWithDetails – fullProductId={}", productId);

        Product product = null;
        try {
            // Check if product already exists; catch exception if not found
            product = productService.getProductById(productId);
        } catch (RuntimeException e) {
            // If exception thrown, assume product does not exist
        }

        if (product != null) {
            log.warn("handleWithDetails – product already exists fullProductId={}", productId);
            throw new ProductAlreadyExistsException("Product already exists");
        }

        // Create and save new product
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

        // Build response with product info
        ProductRespons productRespons = ProductRespons.builder()
                .productId(productRequest.getProductId())
                .productName(product.getProductName())
                .category(product.getCategory())
                .tags(product.getTags())
                .build();
        log.debug("handleWithDetails – returning ProductRespons fullProductId={}", productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(productRespons);
    }

    /**
     * Handles creating a product with default hardcoded values.
     *
     * @param productRequest the request to be populated with default values
     * @param user           the authenticated user
     * @return a ResponseEntity with created default product data
     */
    private ResponseEntity<Object> handleProductOnly(ProductRequest productRequest, User user) {
        // Set default product details if no external source is provided
        log.info("handleProductOnly – setting defaults for userId={}", user.getId());
        productRequest.setProductName("Whitesnake T-shirt");
        productRequest.setCategory("T-shirt");
        productRequest.setTags(List.of("hårdrock", "80-tal", "svart", "bomull"));
        return handleWithDetails(productRequest, user);
    }

    /**
     * Deletes a product and all associated reviews for the authenticated user.
     *
     * @param productId   the client-visible product ID to delete
     * @param userDetails the authenticated user's details
     * @return a ResponseEntity with a success message if deletion succeeds
     * @throws ProductNotFoundException if the product is not found
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Object> deleteProduct(@PathVariable String productId,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        String fullProductId = user.getId() + productId;
        log.info("deleteProduct – productId={}, fullProductId={}, userId={}", productId, fullProductId, user.getId());

        try {
            // Remove related reviews first
            reviewService.deleteReviewsByProductId(fullProductId);
            // Then delete the product itself
            productService.deleteProduct(fullProductId);
            log.info("deleteProduct – deleted product and reviews fullProductId={}", fullProductId);
            return ResponseEntity.ok("Product and related reviews deleted successfully");
        } catch (Exception e) {
            log.warn("deleteProduct – failed to delete fullProductId={}", fullProductId, e);
            throw new ProductNotFoundException("Product not found and not deleted");
        }
    }
}