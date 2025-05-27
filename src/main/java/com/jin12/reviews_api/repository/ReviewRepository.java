package com.jin12.reviews_api.repository;

import com.jin12.reviews_api.model.Review;
import com.jin12.reviews_api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Hämtar alla recensioner för en viss produkt som har skrivits efter ett visst datum.
     *
     * @param product  produkten som recensionerna tillhör
     * @param fromDate från datumet man vill hämta
     * @return lista med matchande recensioner
     */

    List<Review> findByProductAndDateAfter(Product product, LocalDate fromDate);

    // Om vi behöver fler recensioner -> hämta senaste 10 oavsett datum
    List<Review> findTop10ByProductOrderByDateDesc(Product product);
    @Modifying
    @Transactional
    @Query("DELETE FROM Review r WHERE r.product.productId = :productId")
    void deleteByProductId(String productId);
}
