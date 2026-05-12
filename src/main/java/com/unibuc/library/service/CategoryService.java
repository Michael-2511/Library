package com.unibuc.library.service;

import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceInUseException;
import com.unibuc.library.exception.ResourceNotFoundException;
import com.unibuc.library.model.Book;
import com.unibuc.library.model.Category;
import com.unibuc.library.repository.BookRepository;
import com.unibuc.library.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    public CategoryService(CategoryRepository categoryRepository, BookRepository bookRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
    }

    public Category createCategory(Category category) {

        categoryRepository.findByName(category.getName())
                .ifPresent(existingCategory -> {
                    throw new DuplicateResourceException(
                            "Category with name '" + category.getName() + "' already exists"
                    );
                });

        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id
                ));
    }

    public Category updateCategory(Long id, Category category) {
        Category existingCategory = getCategoryById(id);

        categoryRepository.findByName(category.getName())
                .ifPresent(foundCategory -> {
                    if (!foundCategory.getId().equals(id)) {
                        throw new DuplicateResourceException(
                                "Category with name '" + category.getName() + "' already exists"
                        );
                    }
                });

        existingCategory.setName(category.getName());
        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long id) {
        Category existingCategory = getCategoryById(id);

        boolean categoryInUse = bookRepository.findAll().stream()
                .anyMatch(book -> book.getCategory() != null && book.getCategory().getId().equals(id));

        if (categoryInUse) {
            throw new ResourceInUseException(
                    "Category cannot be deleted because it is associated with one or more books"
            );
        }

        categoryRepository.delete(existingCategory);
    }

    public List<Book> getBooksByCategoryName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new RuntimeException("Category name search term cannot be empty");
        }

        String searchTerm = categoryName.toLowerCase().trim();

        return bookRepository.findAll().stream()
                .filter(book -> book.getCategory() != null &&
                        book.getCategory().getName().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }
}
