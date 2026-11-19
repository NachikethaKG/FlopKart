package com.flopkart.product.repository;

import com.flopkart.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Page<Product> findByActiveTrue(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND (:category IS NULL OR LOWER(p.category.slug) = LOWER(:category) OR LOWER(p.category.name) = LOWER(:category))")
    Page<Product> findActiveWithCategoryFilter(@Param("category") String category, Pageable pageable);

    Optional<Product> findByIdAndActiveTrue(Long id);
}
