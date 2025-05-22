package com.jin12.reviews_api.service;

import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;

    // Konstruktor för att injicera produktrepository
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product addProduct(Product product) {
        log.info("addProduct – försök spara produkt: productName={}, category={}", product.getProductName(), product.getCategory());
        Product saved = productRepository.save(product);  // Spara produkten i databasen och returnera den.
        log.info("addProduct – sparad produkt med productId={}", saved.getProductId());
        return saved;
    }

    public void deleteProduct(String productId) {
        log.info("deleteProduct – försök radera produktId={}", productId);
        // Kontrollera om produkten finns innan borttagning
        if (productRepository.existsById(productId)) {
            productRepository.deleteById(productId);  // Ta bort produkten från databasen
            log.info("deleteProduct – produkt raderad produktId={}", productId);
        } else {
            log.warn("deleteProduct – ingen produkt att radera för produktId={}", productId);
        }
    }

    public Product getProductById(String productId) {
        log.debug("getProductById – hämta produkt produktId={}", productId);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    log.warn("getProductById – ingen produkt hittades för produktId={}", productId);
                    return new RuntimeException("Produkt med ID " + productId + " finns inte.");
                }); // Hämta produkten
        log.debug("getProductById – hittade produkt={}", product);
        return product;
    }

    public List<Product> getAllProducts() {
        log.debug("getAllProducts – hämta alla produkter");
        List<Product> list = productRepository.findAll();  // Hämta alla produkter från databasen
        log.debug("getAllProducts – antal produkter={}", list.size());
        return list;
    }

    /**
     * Hämta alla produkter som tillhör en specifik användare.
     *
     * @param userId användarens ID.
     * @return en lista med produkter som tillhör användaren.
     */
    public List<Product> getProductsByUser(Long userId) {
        log.debug("getProductsByUser – hämta produkter för userId={}", userId);
        List<Product> list = productRepository.findByUserId(userId);  // Hämta produkter baserat på användarens ID
        log.debug("getProductsByUser – antal produkter för userId={} = {}", userId, list.size());
        return list;
    }
}