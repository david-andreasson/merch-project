package com.jin12.reviews_api.service;

import com.jin12.reviews_api.exception.ProductAlreadyExistsException;
import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    // Konstruktor för att injicera produktrepository
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product addProduct(Product product) {
        if (productRepository.findByProductId(product.getProductId()).isPresent()) {
            throw new ProductAlreadyExistsException("Produkt med ID " + product.getProductId() + " finns redan.");
        }
        return productRepository.save(product);  // Spara produkten i databasen och returnera den.
    }

    public void deleteProduct(String productId) {
        // Kontrollera om produkten finns innan borttagning
        if (productRepository.existsById(productId)) {
            productRepository.deleteById(productId);  // Ta bort produkten från databasen
        }
    }

    public Product getProductById(String productId) {
        return productRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Produkt med ID " + productId + " finns inte.")); // Hämta produkten
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();  // Hämta alla produkter från databasen
    }

    /**
     * Hämta alla produkter som tillhör en specifik användare.
     *
     * @param userId användarens ID.
     * @return en lista med produkter som tillhör användaren.
     */
    public List<Product> getProductsByUser(Long userId) {
        return productRepository.findByUserId(userId);  // Hämta produkter baserat på användarens ID
    }
}
