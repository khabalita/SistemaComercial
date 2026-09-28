package com.khabalita.sistemacomercial.Repositories;

import com.khabalita.sistemacomercial.Entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends BaseRepository<Product, Long> {

    @EntityGraph(attributePaths = {"coin", "ivaType", "category", "brand", "provider"})
    @Override
    List<Product> findAll();

    @EntityGraph(attributePaths = {"coin", "ivaType", "category", "brand", "provider"})
    @Override
    Optional<Product> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"coin", "ivaType", "category", "brand", "provider"})
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"coin", "ivaType", "category", "brand", "provider"})
    List<Product> findByNameContainingIgnoreCase(String name);

    @EntityGraph(attributePaths = {"coin", "ivaType", "category", "brand", "provider"})
    List<Product> findByInternalCodeContainingIgnoreCase(String internalCode);

    @EntityGraph(attributePaths = {"coin", "ivaType", "category", "brand", "provider"})
    List<Product> findByBrand_Id(Long brandId);

    @EntityGraph(attributePaths = {"coin", "ivaType", "category", "brand", "provider"})
    List<Product> findByCategory_NameContainingIgnoreCase(String categoryName);

    @EntityGraph(attributePaths = {"coin", "ivaType", "category", "brand", "provider"})
    List<Product> findByBrand_NameContainingIgnoreCase(String brandName);

    @EntityGraph(attributePaths = {"coin", "ivaType", "category", "brand", "provider"})
    List<Product> findByProvider_NameContainingIgnoreCase(String providerName);

    @EntityGraph(attributePaths = {"coin", "ivaType", "category", "brand", "provider"})
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @EntityGraph(attributePaths = {"coin", "ivaType", "category", "brand", "provider"})
    Page<Product> findByCategory_NameContainingIgnoreCase(String categoryName, Pageable pageable);

    @EntityGraph(attributePaths = {"coin", "ivaType", "category", "brand", "provider"})
    Page<Product> findByBrand_NameContainingIgnoreCase(String brandName, Pageable pageable);

    @EntityGraph(attributePaths = {"coin", "ivaType", "category", "brand", "provider"})
    Page<Product> findByProvider_NameContainingIgnoreCase(String providerName, Pageable pageable);

    @EntityGraph(attributePaths = {"coin", "ivaType", "category", "brand", "provider"})
    Page<Product> findAllBy(Pageable pageable);

    Product findByBarCode(String barCode);

    Optional<Product> findByInternalCodeIgnoreCase(String internalCode);
}
