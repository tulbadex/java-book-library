package bookstore.controller;

import bookstore.dto.AuthorDTO;
import bookstore.dto.UpdateAuthorDTO;
import bookstore.models.Author;
import bookstore.service.AuthorService;
import jakarta.validation.Valid;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @GetMapping("/add")
    public String addAuthorForm(Model model) {
        model.addAttribute("author", new AuthorDTO());
        return "author/add";
    }

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
    public String editAuthor(@PathVariable("id") UUID id,
                            // @Valid @ModelAttribute("author") UpdateAuthorDTO authorDTO,
                            @Valid @ModelAttribute("author") UpdateAuthorDTO authorDTO,
                            BindingResult result,
                            RedirectAttributes redirectAttributes,
                            Model model,
                            @RequestParam("image") MultipartFile imageFile) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Author - Company name");
            return "author/edit";
        }
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                authorService.updateAuthor(id, authorDTO, imageFile);
            } else {
                authorService.updateAuthor(id, authorDTO, null);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Author updated successfully!");
            return "redirect:/authors/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating author: " + e.getMessage());
            model.addAttribute("author", authorDTO);
            return "author/edit";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteAuthor(@PathVariable UUID id, 
                                @RequestParam(defaultValue = "1") int page,
                                RedirectAttributes redirectAttributes) {
        Author author = authorService.findAuthorById(id);

        // Attempt to delete the image file if it exists
        if (author.getImageUrl() != null) {
            try {
                // Normalize the path by removing leading slash if present
                String relativePath = author.getImageUrl().startsWith("/") 
                                    ? author.getImageUrl().substring(1) 
                                    : author.getImageUrl();
                
                Path imagePath = Paths.get(relativePath); // Create Path from normalized string
                if (Files.exists(imagePath)) {
                    Files.delete(imagePath); // Delete the file if it exists
                }
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error deleting author's image: " + e.getMessage());
            }
        }

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
