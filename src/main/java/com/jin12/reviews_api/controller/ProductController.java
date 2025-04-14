package com.jin12.reviews_api.controller;

import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.model.ProductInfo;
import com.jin12.reviews_api.model.User;
import com.jin12.reviews_api.service.ProductService;
import com.jin12.reviews_api.service.UserService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<Product> addProducts(@RequestBody ProductInfo productInfo,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;

        Product product;
        switch (productInfo.getMode()) {
            case "productOnly":
                product = handleProductOnly(productInfo, user);
                break;
            case "withUrl":
                break;
            case "withDetails":
                break;
            case "customReview":
                break;
            default:
                product = new Product();
        }
        //TODO return valuable information
        return ResponseEntity.ok(null);
    }

    private Product handleProductOnly(ProductInfo productInfo, User user) {
        String productId = user.getId() + productInfo.getProductId();
        Product product;

        try {
            product = productService.getProductById(productId);
            return product;
        } catch (Exception e) {
            //TODO random data till skapandet av product
            product = new Product().builder()
                    .productId(productId)
                    .productName("Whitesnake T-shirt")
                    .category("T-shirt")
                    .tags("hårdrock, 80-tal, svart, bomull")
                    .user(user)
                    .build();
            productService.addProduct(product);
        }
        //TODO: Skicka till review

        return product;
    }
}
