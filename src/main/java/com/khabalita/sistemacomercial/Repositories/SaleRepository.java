package com.khabalita.sistemacomercial.Repositories;

import com.khabalita.sistemacomercial.Entities.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends BaseRepository<Sale, Long> {

    @EntityGraph(attributePaths = {"customer", "items", "items.product",
            "items.product.coin", "items.product.ivaType"}) // Resuelve el problema N + 1
    @Override
    List<Sale> findAll();

    @EntityGraph(attributePaths = {"customer", "items", "items.product"}) // Resuelve el problema N + 1
    @Override
    Optional<Sale> findById(Long id);

    @EntityGraph(attributePaths = {"customer", "items", "items.product"}) // Resuelve el problema N + 1
    List<Sale> findByCustomerId(Long customerId);

    @EntityGraph(attributePaths = {"customer", "items", "items.product"})
    List<Sale> findDistinctByItems_Product_Id(Long productId);

    @EntityGraph(attributePaths = {"customer", "items", "items.product"})
    List<Sale> findByCustomer_NameContainingIgnoreCase(String customerName);

    @EntityGraph(attributePaths = {"customer", "items", "items.product"})
    List<Sale> findDistinctByItems_Product_NameContainingIgnoreCase(String productName);

    @EntityGraph(attributePaths = {"customer", "items", "items.product"})
    List<Sale> findByIdIn(Collection<Long> ids);

    @Query(value = """
            select s.id from Sale s
            left join s.customer c
            left join s.items i
            left join i.product p
            where (:customerName is null or :customerName = '' or
                   lower(c.name) like lower(concat('%', :customerName, '%')))
              and (:productName is null or :productName = '' or
                   lower(p.name) like lower(concat('%', :productName, '%')))
            group by s.id
            order by max(s.date) desc, s.id desc
            """,
            countQuery = """
                    select count(s) from Sale s
                    left join s.customer c
                    left join s.items i
                    left join i.product p
                    where (:customerName is null or :customerName = '' or
                           lower(c.name) like lower(concat('%', :customerName, '%')))
                      and (:productName is null or :productName = '' or
                           lower(p.name) like lower(concat('%', :productName, '%')))
                    """)
    Page<Long> findIdsByFilters(@Param("customerName") String customerName,
                                @Param("productName") String productName,
                                Pageable pageable);

    Optional<Sale> findFirstByOrderByIdDesc();
}
