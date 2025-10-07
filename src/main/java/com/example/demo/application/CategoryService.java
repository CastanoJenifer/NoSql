package com.example.demo.application;


import com.example.demo.controllers.domain.entity.Author;
import com.example.demo.controllers.domain.entity.Categories;
import com.example.demo.controllers.domain.repository.CategoriesRepository;
import com.example.demo.controllers.exception.AuthorNotFoundException;
import com.example.demo.controllers.exception.CategoryNotFoundException;
import com.example.demo.controllers.response.AuthorResponse;
import com.example.demo.controllers.response.BookResponse;
import com.example.demo.controllers.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoriesRepository categoriesRepository;
    private final CacheManager cacheManager;

    @Transactional(readOnly = true)
    @Cacheable(value="categories")
    public Set<CategoryResponse> getAllCategories() {
        return categoriesRepository.findAll().stream()
                .map(this::mapToCategoriesResponse)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    @Cacheable(value="categoryById")
    public CategoryResponse getCategoryById(String id) {
        return categoriesRepository.findById(id)
                .map(this::mapToCategoriesResponse)
                .orElseThrow(() -> new CategoryNotFoundException("Categoría no encontrada con ID: " + id));
    }


    @Transactional
    public Categories updateGenreDescription(String name, String description) {
        cacheManager.getCache("categories").clear();

        Categories genre = categoriesRepository.findByName(name)
                .orElseThrow(() -> new CategoryNotFoundException("Género no encontrado: " + name));

        genre.setDescription(description);
        return categoriesRepository.save(genre);
    }

    private CategoryResponse mapToCategoriesResponse(Categories category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
