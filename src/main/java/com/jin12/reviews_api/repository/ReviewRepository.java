package com.jin12.reviews_api.repository;

import com.jin12.reviews_api.model.Review;
import com.jin12.reviews_api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Hämtar alla recensioner för en viss produkt som har skrivits efter ett visst datum.
     * Användbart för att hämta de senaste 2 månadernas recensioner.
     *
     * @param product produkten som recensionerna tillhör
     * @param fromDate datumgräns (ex. LocalDate.now().minusMonths(2))
     * @return lista med matchande recensioner
     */

    List<Review> findByProductAndDateAfter(Product product, LocalDate fromDate);

}
