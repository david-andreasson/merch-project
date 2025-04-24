package com.jin12.reviews_api.controller;

import com.jin12.reviews_api.dto.ProductRespons;
import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.dto.ProductRequest;
import com.jin12.reviews_api.model.User;
import com.jin12.reviews_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
//    private final UserService userService;

    //TODO implement @GetMapping

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
                break;
            default:
//                respons = new Product();
        }

        return respons;
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
            product = new Product().builder()
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
