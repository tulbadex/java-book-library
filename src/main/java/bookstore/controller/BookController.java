
package bookstore.controller;

import bookstore.models.Book;
import bookstore.dto.BookDTO;
import bookstore.models.Author;
import bookstore.models.Category;
import bookstore.service.BookService;
import bookstore.service.AuthorService;
import bookstore.service.CategoryService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final AuthorService authorService;
    private final CategoryService categoryService;

    @Autowired
    public BookController(BookService bookService, AuthorService authorService, CategoryService categoryService) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.categoryService = categoryService;
    }

    // Display the list of all books
    // No @PreAuthorize annotation here since this method should be accessible to all users
    @GetMapping("/list")
    public String listBooks(Model model, @RequestParam(defaultValue = "1") int page) {
        // Ensure the page is at least 1
        if (page < 1) {
            page = 1;
        }

        Page<Book> booksPage = bookService.getPaginatedBooks(page);

        // Check if the requested page exceeds the total pages
        if (page > booksPage.getTotalPages() && booksPage.getTotalPages() > 0) {
            page = booksPage.getTotalPages(); // Redirect to the last page if necessary
            booksPage = bookService.getPaginatedBooks(page); // Fetch again
        }

        model.addAttribute("books", booksPage.getContent());
        model.addAttribute("page", page);
        model.addAttribute("totalPages", booksPage.getTotalPages());
        model.addAttribute("pageTitle", "Book List - Company name");
        return "book/list"; // Renders the book listing page
    }

    // Display the form for adding a new book
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new BookDTO());
        this.populateAuthorsAndCategories(model);
        model.addAttribute("pageTitle", "Add new book - Company name");
        return "book/add"; // This will render the add book form
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public String addBook(@Valid @ModelAttribute("book") BookDTO bookDTO,
                        BindingResult result,
                        @RequestParam("image") MultipartFile imageFile,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        // imageUrl

        if (result.hasErrors()) {
            model.addAttribute("book", bookDTO);
            this.populateAuthorsAndCategories(model);
            return "book/add";
        }

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                bookService.addBook(bookDTO, imageFile);
            } else {
                bookService.addBook(bookDTO, null);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Book added successfully!");
            return "redirect:/books/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error adding book: " + e.getMessage());
            this.populateAuthorsAndCategories(model);
            return "book/add";
        }
    }

    // Edit a book by ID (for updating a book)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{id}")
    public String showEditBookForm(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Book book = bookService.findBookById(id);
            if (book == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Book not found!");
                return "redirect:/books/list";
            }
            model.addAttribute("book", book);
            this.populateAuthorsAndCategories(model);
            model.addAttribute("pageTitle", "Edit Book - Company name");
            return "book/edit"; // This will render the edit book form
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error fetching book details: " + e.getMessage());
            return "redirect:/books/list";
        }
    }

    // Handle form submission for updating a book
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/edit/{id}")
    public String updateBook(@PathVariable UUID id, 
                            @Valid @ModelAttribute("book") BookDTO book,
                            // @RequestParam("authorId") UUID authorId,
                            // @RequestParam("categoryId") UUID categoryId,
                            @RequestParam(value = "image", required = false) MultipartFile imageFile,
                            BindingResult result,
                            RedirectAttributes redirectAttributes,
                            Model model) throws IOException {

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Book - Company Name");
            this.populateAuthorsAndCategories(model);
            return "book/edit";
        }

        try {
            // Update author logic
            bookService.updateBook(id, book, (imageFile == null || imageFile.isEmpty()) ? null : imageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Book updated successfully!");
            return "redirect:/books/list";
        } catch (Exception e) {
            model.addAttribute("book", book);
            model.addAttribute("errorMessage", "Error updating book: " + e.getMessage());
            this.populateAuthorsAndCategories(model);
            return "book/edit/"; // Stay on the same page to show the error.
        }
    }

    // Delete a book by ID
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteBook(@PathVariable UUID id, @RequestParam(defaultValue = "1") int page, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("successMessage", "Book deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete book: " + e.getMessage());
        }
        
        if (page > 1) {
            return "redirect:/books/list?page=" + page;
        }
        return "redirect:/books/list";
    }

    private void populateAuthorsAndCategories(Model model) {
        model.addAttribute("authors", authorService.getAllAuthors());
        model.addAttribute("categories", categoryService.getAllCategories());
    }
}
