package com.khabalita.sistemacomercial.Repositories;

import com.khabalita.sistemacomercial.Entities.Customer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends BaseRepository<Customer, Long> {

    @EntityGraph(attributePaths = {})
    @Override
    List<Customer> findAll();

    Optional<Customer> findByTaxId(String taxId);

    List<Customer> findByActiveTrue();

    List<Customer> findByNameContainingIgnoreCase(String name);

    @EntityGraph(attributePaths = {})
    Page<Customer> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @EntityGraph(attributePaths = {})
    Page<Customer> findAllBy(Pageable pageable);
}
