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
    private final UserService userService;

    @PostMapping
    public ResponseEntity<List<Product>> addProducts(@RequestBody ProductInfo productInfo) {
        Product product;
        switch (productInfo.getMode()) {
            case "productOnly":
                product = handleProductOnly(productInfo);
                break;
            case "withUrl":
                break;
            case "withDetails":
                break;
            case "customReview":
                break;
            default:
        }
        return ResponseEntity.ok(List.of());
    }

    private Product handleProductOnly(ProductInfo productInfo) {
        //TODO unik nyckel för Product blir userId + productId
        //TODO ta reda på hur man kan få tag på userId
        Product product = new Product();
//        product =

//        productService.getProductById();
        return product;
    }
}
