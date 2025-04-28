package com.jin12.reviews_api.controller;

import com.jin12.reviews_api.dto.*;
import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.model.Review;
import com.jin12.reviews_api.model.User;
import com.jin12.reviews_api.service.ProductService;
import com.jin12.reviews_api.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ReviewService reviewService;
//    private final UserService userService;

    //TODO implement @GetMapping

    /***
     * TODO Är det mening att vi ska returnare alla reviews som finns för produkten vid varje
     * TODO scenario, eller är det meningen att ha en Get /product som får in id och sedan returnerar
     * TODO alla reviews som finns
    ***/
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
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Product do not exist");
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
            return ResponseEntity.badRequest().body("Product do not exist");
        }
    }

    private ResponseEntity<Object> handleWithDetails(ProductRequest productRequest, User user) {
        String productId = user.getId() + productRequest.getProductId();
        Product product;

        try {
            product = productService.getProductById(productId);
            return ResponseEntity.badRequest().body("Product already exists");
        } catch (Exception e) {
            StringBuilder tags = new StringBuilder();
            for (String tag : productRequest.getTags()) {
                tags.append(tag).append(", ");
            }
            product = Product.builder()
                    .productId(productId)
                    .productName(productRequest.getProductName())
                    .category(productRequest.getCategory())
                    .tags(tags.toString())
                    .user(user)
                    .build();
            productService.addProduct(product);

        }
        //TODO: Skicka till review

        ProductRespons productRespons = ProductRespons.builder()
                .productId(product.getProductId())
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
}
