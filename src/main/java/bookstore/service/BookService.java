
package bookstore.service;

import bookstore.dto.BookDTO;
import bookstore.dto.UpdateAuthorDTO;
import bookstore.models.Author;
import bookstore.models.Book;
import bookstore.models.Category;
import bookstore.repository.BookRepository;
import bookstore.repository.AuthorRepository;
import bookstore.repository.CategoryRepository;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    // private final AuthorService authorService;
    // private final CategoryService categoryService;
    private final static String UPLOAD_DIR = "uploads/books";

    @Autowired
    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
    }

    // Create a new book
    @Transactional
    @CacheEvict(value = "books", allEntries = true) // Clear paginated caches
    @CachePut(value = "books", key = "#result.id") 
    public Book addBook(@Valid BookDTO bookDTO, MultipartFile file) {

        if (bookRepository.existsByTitle(bookDTO.getTitle())) {
            throw new IllegalArgumentException("Book title already exists!");
        }

        try {
            Book book = new Book();
            book.setTitle(bookDTO.getTitle());
            book.setDescription(bookDTO.getDescription());
            book.setIsbn(bookDTO.getIsbn());

            Author author = authorRepository.findById(bookDTO.getAuthorId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid author ID"));
            // authorService.findAuthorById(bookDTO.getAuthorId());
            book.setAuthor(author);

            // Set category
            // Category category = categoryService.findCategoryById(bookDTO.getCategoryId());
            Category category = categoryRepository.findById(bookDTO.getCategoryId()).orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));
            book.setCategory(category);

            Book saveBook = bookRepository.save(book);
            if (file != null && !file.isEmpty()) {
                this.uploadBookImage(saveBook.getId(), file);
            }
            
            return saveBook;

        } catch (DataAccessException | IOException e) {
            throw new RuntimeException("Error creating book: " + e.getMessage());
        }
    }

    // Find a book by its ID
    // @Cacheable(value = "books", key = "#id")
    // public Optional<Book> findBookById(UUID id) {
    //     return bookRepository.findById(id);
    // }

    // Cache individual author by ID
    @Cacheable(value = "books", key = "#id")
    public Book findBookById(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid book ID"));
    }

    // Find books by author
    @Cacheable(value = "books", key = "#id")
    public List<Book> findBooksByAuthor(UUID authorId) {
        return bookRepository.findByAuthor_Id(authorId);
    }

    // Find books by category
    @Cacheable(value = "books", key = "#id")
    public List<Book> findBooksByCategory(UUID categoryId) {
        return bookRepository.findByCategory_Id(categoryId);
    }

    // Find a book by its ISBN
    @Cacheable(value = "books", key = "#id")
    public Book findBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    // Update an existing book
    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public Book updateBook(UUID id, @Valid BookDTO updatedBook, MultipartFile file) {
        Book book = findBookById(id);

        try {
            book.setTitle(updatedBook.getTitle());
            book.setIsbn(updatedBook.getIsbn());
            book.setDescription(updatedBook.getDescription());
            Author author = authorRepository.findById(updatedBook.getAuthorId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid author ID"));
            book.setAuthor(author);

            // Set category
            Category category = categoryRepository.findById(updatedBook.getCategoryId()).orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));
            book.setCategory(category);
            // Handle image update
            if (file != null && !file.isEmpty()) {
                // Delete old image if it exists
                if (book.getImageUrl() != null) {
                    String relativePath = book.getImageUrl().startsWith("/") 
                                    ? book.getImageUrl().substring(1) 
                                    : book.getImageUrl();
                
                    Path imagePath = Paths.get(relativePath);
                    Files.deleteIfExists(imagePath);
                }

                // Upload new image
                this.uploadBookImage(id, file);
            }
            return bookRepository.save(book);
        } catch (DataAccessException | IOException e) {
            throw new RuntimeException("Error updating book: " + e.getMessage());
        }
        /* return bookRepository.findById(id).map(book -> {
            book.setTitle(updatedBook.getTitle());
            book.setIsbn(updatedBook.getIsbn());
            // book.setBorrowed(updatedBook.isBorrowed());
            // book.setImageUrl(updatedBook.getImageUrl());
            if (file != null && !file.isEmpty()) {
                this.uploadBookImage(updatedBook.getId(), file);
            }
            // book.setPublishedDate(updatedBook.getPublishedDate());
            book.setDescription(updatedBook.getDescription());
            // book.setAuthor(updatedBook.getAuthor());
            // book.setCategory(updatedBook.getCategory());
            return bookRepository.save(book);
        }).orElseThrow(() -> new IllegalArgumentException("Book with ID " + id + " not found.")); */
    }

    // Delete a book by its ID
    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public void deleteBook(UUID id) {
        Book book = findBookById(id);

        if (!bookRepository.existsById(id)) {
            throw new IllegalArgumentException("Book with ID " + id + " not found.");
        }

        if (book.getImageUrl() != null) {
            try {
                // Normalize the path by removing leading slash if present
                String relativePath = book.getImageUrl().startsWith("/") 
                                    ? book.getImageUrl().substring(1) 
                                    : book.getImageUrl();
                
                Path imagePath = Paths.get(relativePath); // Create Path from normalized string
                if (Files.exists(imagePath)) {
                    Files.delete(imagePath); // Delete the file if it exists
                }
            } catch (IOException e) {
                throw new RuntimeException("Error deleting book's image: " + e.getMessage());
            }
        }

        try {
            bookRepository.deleteById(id);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error deleting book: " + e.getMessage());
        }
    }

    // Get all books
    @Cacheable(value = "books", key = "'all'")
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Async
    public void uploadBookImage(UUID bookId, MultipartFile file) throws IOException {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new IllegalArgumentException("Book not found"));

        this.validateFile(file);
        String fileName = bookId.toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, file.getBytes());

        // Update book with image URL
        book.setImageUrl("/" + UPLOAD_DIR + "/" + fileName);
        bookRepository.save(book);
    }

    // Cache paginated books by page number
    @Cacheable(value = "books", key = "'page_' + #page")
    public Page<Book> getPaginatedBooks(int page) {
        int pageSize = 20; // Define page size
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return bookRepository.findAll(pageable);
    }

    private void validateFile(MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            if (!file.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("Uploaded file must be an image.");
            }
            if (file.getSize() > 5 * 1024 * 1024) { // 5MB size limit
                throw new IllegalArgumentException("File size must not exceed 5MB.");
            }
        }
    }
}
