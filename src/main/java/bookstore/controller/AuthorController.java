package bookstore.controller;

import bookstore.dto.AuthorDTO;
import bookstore.models.Author;
import bookstore.service.AuthorService;
import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/authors")
public class AuthorController {
    @Autowired
    private AuthorService authorService;

    @GetMapping("/list")
    public String listAuthors(@RequestParam(defaultValue = "1") int page, Model model) {
        model.addAttribute("authors", authorService.getPaginatedAuthors(page));
        model.addAttribute("page", page);
        model.addAttribute("totalPages", authorService.getTotalPages());
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

    @PostMapping("/authors")
    public String createAuthor(@Valid AuthorDTO authorDTO, @RequestParam("image") MultipartFile imageFile, Model model) {
        try {
            Author author = authorService.createAuthor(authorDTO, imageFile);
            model.addAttribute("author", author);
            return "redirect:/authors/list";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "author/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String editAuthorForm(@PathVariable("id") UUID id, Model model) {
        model.addAttribute("author", authorService.findAuthorById(id));
        return "author/edit";
    }

    @PostMapping("/edit/{id}")
    public String editAuthor(@PathVariable("id") UUID id,
                             @Valid @ModelAttribute("author") AuthorDTO authorDTO,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "author/edit";
        }
        return "redirect:/author/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteAuthor(@PathVariable("id") UUID id) {
        authorService.deleteAuthor(id);
        return "redirect:/author/list";
    }
}
