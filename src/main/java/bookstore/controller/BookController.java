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
import java.util.List;
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
        Page<Book> booksPage = bookService.getPaginatedBooks(page);
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
        model.addAttribute("book", new Book());
        model.addAttribute("authors", authorService.getAllAuthors()); // Load authors for dropdown
        model.addAttribute("categories", categoryService.getAllCategories()); // Load categories for dropdown
        model.addAttribute("pageTitle", "Add new book - Company name");
        return "book/add"; // This will render the add book form
    }

    // // Handle form submission for adding a new book
    // @PreAuthorize("hasRole('ADMIN')")
    // @PostMapping("/add")
    // public String addBook(@Valid @ModelAttribute BookDTO bookDTO, 
    //                     BindingResult result,
    //                     @RequestParam("image") MultipartFile imageFile,
    //                     RedirectAttributes redirectAttributes) throws IOException {
    //     if (result.hasErrors()) {
    //         result.getFieldErrors().forEach(error -> 
    //             redirectAttributes.addFlashAttribute("errors", Map.of(error.getField(), error.getDefaultMessage())));
    //         return "redirect:/books/add";
    //     }

    //     try {
    //         // Create a new Book entity and set its properties
    //         Book book = new Book();
    //         book.setTitle(bookDTO.getTitle());
    //         book.setDescription(bookDTO.getDescription());

    //         // Find the author by ID if provided
    //         if (bookDTO.getAuthorId() != null) {
    //             Author author = authorService.findAuthorById(bookDTO.getAuthorId());
    //             book.setAuthor(author);
    //         }

    //         // Find the category by ID
    //         Category category = categoryService.findCategoryById(bookDTO.getCategoryId());
    //         book.setCategory(category);

    //         // Save the book and upload the image asynchronously
    //         Book savedBook = bookService.addBook(book);
    //         bookService.uploadBookImage(savedBook.getId(), imageFile); // Upload image

    //         redirectAttributes.addFlashAttribute("successMessage", "Book added successfully!");
    //         return "redirect:/books/list";
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("errorMessage", "Error adding book: " + e.getMessage());
    //         return "redirect:/books/add";
    //     }
    // }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public String addBook(@Valid @ModelAttribute("book") BookDTO bookDTO,
                        BindingResult result,
                        @RequestParam("image") MultipartFile imageFile,
                        Model model) {
        if (result.hasErrors()) {
            model.addAttribute("authors", authorService.getAllAuthors()); // Repopulate dropdowns
            model.addAttribute("categories", categoryService.getAllCategories());
            return "books/add";
        }

        try {
            // Create a new Book entity and set its properties
            Book book = new Book();
            book.setTitle(bookDTO.getTitle());
            book.setDescription(bookDTO.getDescription());
            book.setIsbn(bookDTO.getIsbn());

            // Set author if ID is provided
            if (bookDTO.getAuthorId() != null) {
                Author author = authorService.findAuthorById(bookDTO.getAuthorId());
                book.setAuthor(author);
            }

            // Set category
            Category category = categoryService.findCategoryById(bookDTO.getCategoryId());
            book.setCategory(category);

            // Save book and upload image
            Book savedBook = bookService.addBook(book);
            bookService.uploadBookImage(savedBook.getId(), imageFile);

            model.addAttribute("successMessage", "Book added successfully!");
            return "redirect:/books/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error adding book: " + e.getMessage());
            model.addAttribute("authors", authorService.getAllAuthors());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "books/add";
        }
    }

    // Edit a book by ID (for updating a book)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{id}")
    public String showEditBookForm(@PathVariable UUID id, Model model) {
        Book book = bookService.findBookById(id).orElseThrow(() -> new IllegalArgumentException("Invalid book ID"));
        model.addAttribute("book", book);
        model.addAttribute("authors", authorService.getAllAuthors());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageTitle", "Edit Book - Company name");
        return "book/edit"; // This will render the edit book form
    }

    // Handle form submission for updating a book
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/edit/{id}")
    public String updateBook(@PathVariable UUID id, @ModelAttribute Book book,
                             @RequestParam("authorId") UUID authorId,
                             @RequestParam("categoryId") UUID categoryId,
                             @RequestParam("image") MultipartFile imageFile) throws IOException {
        // Find the author and category by ID
        Author author = authorService.findAuthorById(authorId);
        Category category = categoryService.findCategoryById(categoryId);
        
        // Set author and category in the book
        book.setAuthor(author);
        book.setCategory(category);

        // Update the book and handle image upload
        Book updatedBook = bookService.updateBook(id, book);
        if (!imageFile.isEmpty()) {
            bookService.uploadBookImage(updatedBook.getId(), imageFile); // Upload new image if provided
        }

        return "redirect:/books/list"; // Redirect to book listing after updating
    }

    // Delete a book by ID
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteBook(@PathVariable UUID id) {
        bookService.deleteBook(id);
        return "redirect:/books/list"; // Redirect to book listing after deletion
    }
}
