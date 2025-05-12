package com.jin12.reviews_api.service;

import com.jin12.reviews_api.model.Product;
import com.jin12.reviews_api.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productService = new ProductService(productRepository);
    }

    @Test
    void testAddProduct() {
        Product product = new Product();
        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.addProduct(product);

        assertEquals(product, result);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void testDeleteProductWhenExists() {
        String productId = "abc123";
        when(productRepository.existsById(productId)).thenReturn(true);

        productService.deleteProduct(productId);

        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    void testDeleteProductWhenNotExists() {
        String productId = "notExist";
        when(productRepository.existsById(productId)).thenReturn(false);

        productService.deleteProduct(productId);

        verify(productRepository, never()).deleteById(productId);
    }

    @Test
    void testGetProductByIdWhenExists() {
        String productId = "p1";
        Product product = new Product();
        product.setProductId(productId);
        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(productId);

        assertEquals(product, result);
    }

    @Test
    void testGetProductByIdWhenNotExists() {
        String productId = "missing";
        when(productRepository.findByProductId(productId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                productService.getProductById(productId));

        assertTrue(exception.getMessage().contains("finns inte"));
    }

    @Test
    void testGetAllProducts() {
        Product p1 = new Product();
        Product p2 = new Product();
        List<Product> products = Arrays.asList(p1, p2);
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.getAllProducts();

        assertEquals(2, result.size());
        assertEquals(products, result);
    }

    @Test
    void testGetProductsByUser() {
        Long userId = 42L;
        Product p1 = new Product();
        Product p2 = new Product();
        List<Product> userProducts = Arrays.asList(p1, p2);
        when(productRepository.findByUserId(userId)).thenReturn(userProducts);

        List<Product> result = productService.getProductsByUser(userId);

        assertEquals(2, result.size());
        assertEquals(userProducts, result);
    }
}
