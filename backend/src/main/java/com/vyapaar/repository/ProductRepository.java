package com.vyapaar.repository;

import com.vyapaar.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrue();

    /**
     * Adjusts stock inside the database instead of reading the value into Java, changing it and
     * writing it back. That read-modify-write pattern loses an update when two sales are recorded
     * at the same moment: both read 100, both compute 88, and the second write silently discards
     * the first. Here the database does the arithmetic in one atomic statement, so concurrent
     * adjustments add up correctly.
     *
     * Returns the number of rows changed, which is 0 when the product id does not exist. Callers
     * check that instead of loading the product first.
     *
     * Note: a bulk update bypasses the persistence context, so any Product entity already loaded
     * in this transaction holds a stale stockQuantity. Neither caller reads it afterwards.
     */
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :delta WHERE p.id = :id")
    int adjustStock(@Param("id") Long id, @Param("delta") Double delta);

    /** Same, plus the restock date, for a purchase. */
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :delta, "
            + "p.lastRestockedDate = :restockedOn WHERE p.id = :id")
    int adjustStockAndRestockDate(@Param("id") Long id,
                                  @Param("delta") Double delta,
                                  @Param("restockedOn") LocalDate restockedOn);
}
