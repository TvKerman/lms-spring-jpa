package com.example.lms.service;

import com.example.lms.entity.Category;
import com.example.lms.exception.DuplicateResourceException;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category create(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new DuplicateResourceException("Category '" + name + "' already exists");
        }
        Category category = new Category();
        category.setName(name);
        Category saved = categoryRepository.save(category);
        log.info("Created category id={}, name={}", saved.getId(), name);
        return saved;
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public void delete(Long id) {
        Category category = findById(id);
        categoryRepository.delete(category);
        log.info("Deleted category id={}", id);
    }
}