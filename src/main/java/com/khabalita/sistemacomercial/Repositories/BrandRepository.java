package com.khabalita.sistemacomercial.Repositories;

import com.khabalita.sistemacomercial.Entities.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BrandRepository extends BaseRepository<Brand, Long> {

    Brand findByNameIgnoreCase(String name);

    List<Brand> findByNameContainingIgnoreCase(String name);

    Page<Brand> findAllBy(Pageable pageable);

    Page<Brand> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
