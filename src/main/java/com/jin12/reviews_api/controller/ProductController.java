package com.jin12.reviews_api.controller;

import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.model.ProductInfo;
import com.jin12.reviews_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<List<Product>> addProducts(@RequestBody ProductInfo productInfo) {
        Product product;
        switch (productInfo.getMode()) {
            case "productOnly":
                product = new Product();
                product.setProductId(productInfo.getProductId());
                product.setProductName("Placeholder");
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

//    private Product handleProductOnly(ProductInfo productInfo) {
//
//        productService.getProductById();
//    }
}
