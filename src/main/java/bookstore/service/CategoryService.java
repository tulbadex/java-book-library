package bookstore.service;

import bookstore.models.Category;
import bookstore.repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public boolean isCategoryNameExists(String name) {
        return categoryRepository.existsByName(name);
    }

    @CacheEvict(value = "categories", allEntries = true) // Clear paginated caches
    @CachePut(value = "categories", key = "#result.id") 
    public Category addCategory(Category category) {
        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Cacheable(value = "categories", key = "#id")
    public Category findCategoryById(UUID id) {
        return categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));
    }

    @CacheEvict(value = "categories", allEntries = true) // Clear related caches
    @CachePut(value = "categories", key = "#id")
    public Category updateCategory(UUID id, Category updatedCategory) {
        return categoryRepository.findById(id).map(existingCategory -> {
            if (!existingCategory.getName().equals(updatedCategory.getName())) {
                Optional<Category> categoryWithSameName = categoryRepository.findByName(updatedCategory.getName());
                if (categoryWithSameName.isPresent()) {
                    throw new IllegalArgumentException("Category with name '" + updatedCategory.getName() + "' already exists.");
                }
            }
            existingCategory.setName(updatedCategory.getName());
            return categoryRepository.save(existingCategory);
        }).orElseThrow(() -> new IllegalArgumentException("Category with ID " + id + " not found."));
    }

    @Cacheable(value = "categories", key = "'page_' + #page") // Cache paginated results
    public Page<Category> getPaginatedCategories(int page) {
        int pageSize = 20; // Number of categories per page
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return categoryRepository.findAll(pageable);
    }

    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Category with ID " + id + " not found.");
        }
        categoryRepository.deleteById(id);
    }
}
