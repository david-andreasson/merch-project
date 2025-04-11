package com.jin12.reviews_api.controller;

import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.model.ProductInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<List<Product>> addProducts(@RequestBody ProductInfo productInfo) {
        Product product;
        switch (productInfo.getMode()) {
            case "productOnly":
                product = new Product();
                product.setProductId(productInfo.getProductId());
                break;
            case "withUrl":
                break;
            case "withDetails":
                break;
            case "customReview":
                break;
        }
        return ResponseEntity.ok(List.of());
    }
}
