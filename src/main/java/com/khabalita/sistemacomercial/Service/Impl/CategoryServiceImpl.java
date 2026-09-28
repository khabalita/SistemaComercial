package com.khabalita.sistemacomercial.Service.Impl;

import com.khabalita.sistemacomercial.Entities.Category;
import com.khabalita.sistemacomercial.audit.Audited;
import com.khabalita.sistemacomercial.Repositories.CategoryRepository;
import com.khabalita.sistemacomercial.Service.ICategoryService;
import com.khabalita.sistemacomercial.dto.request.CategoryRequestDto;
import com.khabalita.sistemacomercial.dto.response.CategoryResponseDto;
import com.khabalita.sistemacomercial.mapper.CategoryMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryByName(String name) {
        return categoryMapper.toDto(categoryRepository.findByNameIgnoreCase(name));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllCategories() {
        return categoryMapper.categoryResponseDtoList(categoryRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponseDto> getCategoriesPage(String name, Pageable pageable) {
        Page<Category> page = name == null || name.isBlank()
                ? categoryRepository.findAllBy(pageable)
                : categoryRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
        return page.map(categoryMapper::toDto);
    }

    @Override
    @Transactional
    @Audited("CATEGORY_CREATE")
    public CategoryResponseDto saveCategory(CategoryRequestDto categoryRequestDto) {
        Category category = categoryMapper.toEntity(categoryRequestDto);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    @Audited("CATEGORY_DELETE")
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    @Audited("CATEGORY_UPDATE")
    public CategoryResponseDto updateCategory(Long id, CategoryRequestDto categoryRequestDto) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
        existing.setName(categoryRequestDto.name());
        return categoryMapper.toDto(categoryRepository.save(existing));
    }
}
