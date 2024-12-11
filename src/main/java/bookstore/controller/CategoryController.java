package bookstore.controller;

import bookstore.service.CategoryService;
import jakarta.validation.Valid;
import bookstore.dto.CategoryDTO;
import bookstore.models.Category;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

@Controller
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/list")
    public String listCategories(Model model, @RequestParam(defaultValue = "1") int page) {
        // Ensure the page is at least 1
        if (page < 1) {
            page = 1;
        }

        // Fetch paginated categories
        Page<Category> categoryPage = categoryService.getPaginatedCategories(page);

        // Check if the requested page exceeds the total pages
        if (page > categoryPage.getTotalPages() && categoryPage.getTotalPages() > 0) {
            page = categoryPage.getTotalPages(); // Redirect to the last page if necessary
            categoryPage = categoryService.getPaginatedCategories(page); // Fetch again
        }

        // Add attributes to the model
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("page", page);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("pageTitle", "Category List - Company name");

        return "category/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/add")
    public String showAddCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("pageTitle", "Add new category - Company name");
        return "category/add"; // This will render the add category form
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public String addCategory(
            @Valid @ModelAttribute("category") CategoryDTO categoryDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("category", categoryDTO);
            return "category/add";
        }

        try {
            // Check for duplicate category names
            if (categoryService.isCategoryNameExists(categoryDTO.getName())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Category name already exists.");
                return "redirect:/category/add";
            }

            Category category = new Category();
            category.setName(categoryDTO.getName());

            // Add the new category
            categoryService.addCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Category added successfully!");
            return "redirect:/category/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding category: " + e.getMessage());
            return "redirect:/category/add";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{id}")
    public String showEditCategoryForm(@PathVariable UUID id, Model model) {
        // Category category = categoryService.findCategoryById(id).orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));
        Category category = categoryService.findCategoryById(id);
        model.addAttribute("category", category);
        model.addAttribute("pageTitle", "Edit Category - Company name");
        return "category/edit"; // This will render the edit book form
    }

    // Handle form submission for updating a category
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable UUID id, @ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            // Call the service to update the category
            categoryService.updateCategory(id, category);
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successful!");
            return "redirect:/category/list";
            // return "redirect:/category/list"; // Redirect after successful update
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // Handle the error and redirect back to the edit form with an error message
            return "redirect:/category/list";
        }
    }

    // Delete a book by ID
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable UUID id, 
                @RequestParam(defaultValue = "1") int page,
                RedirectAttributes redirectAttributes
    ) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete category: " + e.getMessage());
        }
        if (page > 1) {
            return "redirect:/category/list?page=" + page;
        }
        return "redirect:/category/list";
    }
}
