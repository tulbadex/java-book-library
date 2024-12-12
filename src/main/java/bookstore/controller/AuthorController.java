package bookstore.controller;

import bookstore.dto.AuthorDTO;
import bookstore.dto.UpdateAuthorDTO;
import bookstore.models.Author;
import bookstore.service.AuthorService;
import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/authors")
public class AuthorController {
    @Autowired
    private AuthorService authorService;

    @GetMapping("/list")
    public String listAuthors(Model model, @RequestParam(defaultValue = "1") int page) {
        // Ensure the page is at least 1
        if (page < 1) {
            page = 1;
        }

        // Fetch paginated categories
        Page<Author> authorPage = authorService.getPaginatedAuthors(page);

        // Check if the requested page exceeds the total pages
        if (page > authorPage.getTotalPages() && authorPage.getTotalPages() > 0) {
            page = authorPage.getTotalPages(); // Redirect to the last page if necessary
            authorPage = authorService.getPaginatedAuthors(page); // Fetch again
        }

        // Add attributes to the model
        model.addAttribute("authors", authorPage.getContent());
        model.addAttribute("page", page);
        model.addAttribute("totalPages", authorPage.getTotalPages());
        model.addAttribute("pageTitle", "Author List - Company name");

        return "author/list";
    }


    @GetMapping("/{id}")
    public String viewAuthor(@PathVariable("id") UUID id, Model model) {
        model.addAttribute("author", authorService.findAuthorById(id));
        return "author/details";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/add")
    public String addAuthorForm(Model model) {
        model.addAttribute("author", new AuthorDTO());
        return "author/add";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public String createAuthor(@Valid AuthorDTO authorDTO, 
        @RequestParam("image") MultipartFile imageFile, Model model, 
        RedirectAttributes redirectAttributes) 
        {
        try {
            // authorService.createAuthor(authorDTO, imageFile);
            if (imageFile != null && !imageFile.isEmpty()) {
                authorService.createAuthor(authorDTO, imageFile);
            } else {
                authorService.createAuthor(authorDTO, null);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Author added successfully!");
            return "redirect:/authors/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding author: " + e.getMessage());
            return "redirect:/authors/add";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{id}")
    public String editAuthorForm(@PathVariable("id") UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Author author = authorService.findAuthorById(id);
            if (author == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Author not found!");
                return "redirect:/authors/list";
            }
            model.addAttribute("author", author);
            model.addAttribute("pageTitle", "Edit Author - Company name");
            return "author/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error fetching author details: " + e.getMessage());
            return "redirect:/authors/list";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/edit/{id}")
    public String updateAuthor(
            @PathVariable("id") UUID id,
            @Valid @ModelAttribute("author") UpdateAuthorDTO authorDTO,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Author - Company Name");
            return "author/edit";
            // return "redirect:/authors/edit/" + id; // Return the view template for editing.
        }

        try {
            // Update author logic
            authorService.updateAuthor(id, authorDTO, (imageFile == null || imageFile.isEmpty()) ? null : imageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Author updated successfully!");
            return "redirect:/authors/list";
        } catch (Exception e) {
            model.addAttribute("author", authorDTO);
            model.addAttribute("errorMessage", "Error updating author: " + e.getMessage());
            return "author/edit"; // Stay on the same page to show the error.
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/view/{id}")
    public String viewAuthor(@PathVariable("id") UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Author author = authorService.findAuthorById(id);
            if (author == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Author not found!");
                return "redirect:/authors/list";
            }
            model.addAttribute("author", author);
            model.addAttribute("pageTitle", "View Author - Company name");
            return "author/details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error fetching author details: " + e.getMessage());
            return "redirect:/authors/list";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteAuthor(@PathVariable UUID id, 
                                @RequestParam(defaultValue = "1") int page,
                                RedirectAttributes redirectAttributes) {

        try {
            authorService.deleteAuthor(id);
            redirectAttributes.addFlashAttribute("successMessage", "Author deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete author: " + e.getMessage());
        }
        
        if (page > 1) {
            return "redirect:/authors/list?page=" + page;
        }
        return "redirect:/authors/list"; 
    }
}
