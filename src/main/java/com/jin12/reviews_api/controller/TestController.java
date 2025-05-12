package com.jin12.reviews_api.controller;

import com.jin12.reviews_api.dto.ProductInfo;
import com.jin12.reviews_api.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {
    private final Map<String, ProductInfo> products = new HashMap<>();

    @GetMapping
    public ResponseEntity<Object> test(@RequestParam String productId) {
        createProducts();

        if (products.containsKey(productId)) {
            return ResponseEntity.ok(products.get(productId));
        }

        return ResponseEntity.badRequest().body("No product found with id " + productId);
    }

    private void createProducts() {
        ProductInfo productInfo = ProductInfo.builder()
                .productName("Volvo")
                .category("Car")
                .tags(List.of("C30", "Grön", "Halvkombi"))
                .build();
        products.put("T12345", productInfo);

        productInfo = ProductInfo.builder()
                .productName("Ford")
                .category("Car")
                .tags(List.of("Focus", "Svart", "Kombi"))
                .build();
        products.put("T12346", productInfo);

        productInfo = ProductInfo.builder()
                .productName("Volkswagen")
                .category("Car")
                .tags(List.of("Passat", "Silver", "Kombi"))
                .build();
        products.put("T12347", productInfo);
    }
}
