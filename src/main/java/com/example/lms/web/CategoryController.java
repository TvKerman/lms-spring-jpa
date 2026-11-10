package com.example.lms.web;

import com.example.lms.entity.Category;
import com.example.lms.service.CategoryService;
import com.example.lms.web.dto.CategoryResponse;
import com.example.lms.web.dto.CreateCategoryRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> list() {
        return categoryService.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName()))
                .toList();
    }

    @GetMapping("/{id}")
    public CategoryResponse get(@PathVariable Long id) {
        Category c = categoryService.findById(id);
        return new CategoryResponse(c.getId(), c.getName());
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest req) {
        Category c = categoryService.create(req.name());
        return ResponseEntity.created(URI.create("/api/categories/" + c.getId()))
                .body(new CategoryResponse(c.getId(), c.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}