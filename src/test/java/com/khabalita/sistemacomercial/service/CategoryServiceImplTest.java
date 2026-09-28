package com.khabalita.sistemacomercial.service;

import com.khabalita.sistemacomercial.Entities.Category;
import com.khabalita.sistemacomercial.Repositories.CategoryRepository;
import com.khabalita.sistemacomercial.Service.Impl.CategoryServiceImpl;
import com.khabalita.sistemacomercial.dto.request.CategoryRequestDto;
import com.khabalita.sistemacomercial.dto.response.CategoryResponseDto;
import com.khabalita.sistemacomercial.mapper.CategoryMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock CategoryRepository categoryRepository;
    @Mock CategoryMapper categoryMapper;

    @InjectMocks CategoryServiceImpl service;

    @Test
    void getAll_returnsMappedList() {
        when(categoryRepository.findAll()).thenReturn(List.of(new Category()));
        when(categoryMapper.categoryResponseDtoList(any())).thenReturn(List.of(
                CategoryResponseDto.builder().id(1L).name("General").build()));

        assertEquals(1, service.getAllCategories().size());
    }

    @Test
    void getById_existing_returnsDto() {
        Category c = Category.builder().name("General").build();
        c.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c));
        when(categoryMapper.toDto(c)).thenReturn(CategoryResponseDto.builder().id(1L).name("General").build());

        assertEquals("General", service.getCategoryById(1L).name());
    }

    @Test
    void getById_notFound_throws() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getCategoryById(99L));
    }

    @Test
    void save_persistsAndReturns() {
        CategoryRequestDto req = CategoryRequestDto.builder().name("Lacteos").build();
        Category entity = Category.builder().name("Lacteos").build();
        CategoryResponseDto resp = CategoryResponseDto.builder().id(1L).name("Lacteos").build();

        when(categoryMapper.toEntity(req)).thenReturn(entity);
        when(categoryRepository.save(entity)).thenReturn(entity);
        when(categoryMapper.toDto(entity)).thenReturn(resp);

        CategoryResponseDto result = service.saveCategory(req);

        assertEquals("Lacteos", result.name());
        verify(categoryRepository).save(entity);
    }

    @Test
    void update_existing_updatesAndSaves() {
        Category existing = Category.builder().name("General").build();
        existing.setId(1L);
        CategoryRequestDto req = CategoryRequestDto.builder().name("General updated").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(existing);
        when(categoryMapper.toDto(existing))
                .thenReturn(CategoryResponseDto.builder().id(1L).name("General updated").build());

        CategoryResponseDto result = service.updateCategory(1L, req);

        assertEquals("General updated", result.name());
        assertEquals("General updated", existing.getName());
    }

    @Test
    void update_notFound_throws() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.updateCategory(99L, CategoryRequestDto.builder().name("X").build()));
    }

    @Test
    void delete_existing_deletes() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        service.deleteCategory(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throws() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.deleteCategory(99L));
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void getByName_returnsDto() {
        Category c = Category.builder().name("General").build();
        c.setId(1L);
        when(categoryRepository.findByNameIgnoreCase("general")).thenReturn(c);
        when(categoryMapper.toDto(c)).thenReturn(CategoryResponseDto.builder().id(1L).name("General").build());

        assertEquals("General", service.getCategoryByName("general").name());
    }
}
