package com.khabalita.sistemacomercial.mapper;

import com.khabalita.sistemacomercial.dto.request.CategoryRequestDto;
import com.khabalita.sistemacomercial.dto.response.CategoryResponseDto;
import com.khabalita.sistemacomercial.Entities.Category;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryMapper {

    public CategoryResponseDto toDto(Category entity) {
        if (entity == null) return null;
        return CategoryResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public Category toEntity(CategoryRequestDto dto) {
        if (dto == null) return null;
        Category entity = new Category();
        entity.setName(dto.name());
        return entity;
    }

    public List<CategoryResponseDto> categoryResponseDtoList(List<Category> categories) {
        if (categories == null) return List.of();
        return categories.stream()
                .map(this::toDto)
                .toList();
    }
}