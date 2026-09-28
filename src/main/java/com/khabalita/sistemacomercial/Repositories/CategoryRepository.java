package com.khabalita.sistemacomercial.Repositories;

import com.khabalita.sistemacomercial.Entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryRepository extends BaseRepository<Category, Long> {

    Category findByNameIgnoreCase(String name);

    List<Category> findByNameContainingIgnoreCase(String name);

    Page<Category> findAllBy(Pageable pageable);

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
