package org.cezszym.controller;

import org.cezszym.entity.Category;
import org.cezszym.entity.User;
import org.cezszym.enums.Role;
import org.cezszym.jwt.Identity;
import org.cezszym.repository.CategoryRepository;
import org.cezszym.repository.CategoryRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final Identity identity;

    public CategoryController(CategoryRepository categoryRepository, Identity identity) {
        this.categoryRepository = categoryRepository;
        this.identity = identity;
    }

    private Role GetUserRole() {
        User user = identity.getCurrent();
        return user.getRole();
    }

    @GetMapping("/categories")
    ResponseEntity<CollectionModel<Category>>  all() {
        ArrayList<Category> categories = new ArrayList<>(categoryRepository.findAll());
        return ResponseEntity.ok(CollectionModel.of(categories));
    }

    @PostMapping("/categories")
    ResponseEntity<EntityModel<Category>> newCategory(@RequestBody Category newCategory) {
        if (!GetUserRole().equals(Role.ADMIN)) return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        Category category = categoryRepository.save(newCategory);
        return ResponseEntity.ok(EntityModel.of(category));

    }


    @GetMapping("/categories/{id}")
    ResponseEntity<EntityModel<Category>> getCategory(@PathVariable int id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return ResponseEntity.ok(EntityModel.of(category));
    }

    @PutMapping("/categories/{id}")
    ResponseEntity<EntityModel<Category>> replaceEmployee(@RequestBody Category newCategory, @PathVariable int id) {
        if (!GetUserRole().equals(Role.ADMIN)) return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        Category selectedCategory = categoryRepository.findById(id)
                .map(category -> {
                    category.setName(newCategory.getName());
                    return categoryRepository.save(category);
                })
                .orElseGet(() -> {
                    newCategory.setId(id);
                    return categoryRepository.save(newCategory);
                });
        return ResponseEntity.ok(EntityModel.of(selectedCategory));
    }


    @DeleteMapping("/categories/{id}")
    ResponseEntity deleteCategory(@PathVariable int id) {
        if (!GetUserRole().equals(Role.ADMIN)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        categoryRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    class CategoryNotFoundException extends RuntimeException {

        CategoryNotFoundException(int id) {
            super("No category with an id: " + id);
        }
    }

}
