package com.jin12.reviews_api.service;

import com.jin12.reviews_api.exception.ProductAlreadyExistsException;
import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ProductService handles core product operations such as adding, deleting,
 * and fetching products. Ensures no duplicate productId and retrieves products by user.
 */
@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;

    /**
     * Constructor for dependency injection of ProductRepository.
     *
     * @param productRepository repository used to interact with product data
     */
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Adds a new product to the database. Throws an exception if a product
     * with the same productId already exists.
     *
     * @param product the product entity to save
     * @return the saved Product entity
     * @throws ProductAlreadyExistsException if productId is already taken
     */
    public Product addProduct(Product product) {
        log.info("addProduct – försök spara produkt: productName={}, category={}",
                product.getProductName(), product.getCategory());
        // Check if a product with this ID already exists
        if (productRepository.findByProductId(product.getProductId()).isPresent()) {
            throw new ProductAlreadyExistsException("Produkt med ID " + product.getProductId() + " finns redan.");
        }
        // Save the new product
        Product saved = productRepository.save(product);
        log.info("addProduct – sparad produkt med productId={}", saved.getProductId());
        return saved;
    }

    /**
     * Deletes a product by its ID if it exists. Logs a warning if not found.
     *
     * @param productId the fullProductId of the product to delete
     */
    public void deleteProduct(String productId) {
        log.info("deleteProduct – försök radera produktId={}", productId);
        // Only delete if the product actually exists
        if (productRepository.existsById(productId)) {
            productRepository.deleteById(productId);
            log.info("deleteProduct – produkt raderad produktId={}", productId);
        } else {
            log.warn("deleteProduct – ingen produkt att radera för produktId={}", productId);
        }
    }

    /**
     * Retrieves a product by its ID. Throws RuntimeException if not found.
     *
     * @param productId the fullProductId to find
     * @return the found Product entity
     * @throws RuntimeException if no product is found for the given ID
     */
    public Product getProductById(String productId) {
        log.debug("getProductById – hämta produkt produktId={}", productId);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    log.warn("getProductById – ingen produkt hittades för produktId={}", productId);
                    return new RuntimeException("Produkt med ID " + productId + " finns inte.");
                });
        log.debug("getProductById – hittade produkt={}", product);
        return product;
    }

    /**
     * Retrieves all products in the database.
     *
     * @return a list of all Product entities
     */
    public List<Product> getAllProducts() {
        log.debug("getAllProducts – hämta alla produkter");
        List<Product> list = productRepository.findAll();
        log.debug("getAllProducts – antal produkter={}", list.size());
        return list;
    }

    /**
     * Fetches all products that belong to a specific user.
     *
     * @param userId the ID of the user
     * @return a list of Product entities owned by the user
     */
    public List<Product> getProductsByUser(Long userId) {
        log.debug("getProductsByUser – hämta produkter för userId={}", userId);
        List<Product> list = productRepository.findByUserId(userId);
        log.debug("getProductsByUser – antal produkter för userId={} = {}", userId, list.size());
        return list;
    }
}