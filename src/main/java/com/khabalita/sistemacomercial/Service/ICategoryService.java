package com.khabalita.sistemacomercial.Service;

import com.khabalita.sistemacomercial.dto.request.CategoryRequestDto;
import com.khabalita.sistemacomercial.dto.response.CategoryResponseDto;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICategoryService {

    List<CategoryResponseDto> getAllCategories();
    Page<CategoryResponseDto> getCategoriesPage(String name, Pageable pageable);
    CategoryResponseDto getCategoryByName(String name);
    CategoryResponseDto getCategoryById(Long id);
    CategoryResponseDto saveCategory(CategoryRequestDto categoryRequestDto);
    void deleteCategory(Long id);
    CategoryResponseDto updateCategory(Long id, CategoryRequestDto categoryRequestDto);

}
