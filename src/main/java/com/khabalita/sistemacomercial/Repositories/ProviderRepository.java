package com.khabalita.sistemacomercial.Repositories;

import com.khabalita.sistemacomercial.Entities.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProviderRepository extends BaseRepository<Provider, Long> {

    Optional<Provider> findByTaxId(String taxId);

    List<Provider> findByNameContainingIgnoreCase(String name);

    Provider findByNameIgnoreCase(String name);

    Page<Provider> findAllBy(Pageable pageable);

    Page<Provider> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
