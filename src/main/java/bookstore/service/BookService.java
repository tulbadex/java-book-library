package bookstore.service;

import bookstore.models.Book;
import bookstore.repository.BookRepository;
import bookstore.repository.AuthorRepository;
import bookstore.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
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
    private final static String UPLOAD_DIR = "uploads/books/";

    @Autowired
    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
    }

    // Create a new book
    public Book addBook(Book book) {
        return bookRepository.save(book);
    }

    // Find a book by its ID
    public Optional<Book> findBookById(UUID id) {
        return bookRepository.findById(id);
    }

    // Find books by author
    public List<Book> findBooksByAuthor(UUID authorId) {
        return bookRepository.findByAuthor_Id(authorId);
    }

    // Find books by category
    public List<Book> findBooksByCategory(UUID categoryId) {
        return bookRepository.findByCategory_Id(categoryId);
    }

    // Find a book by its ISBN
    public Book findBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    // Update an existing book
    public Book updateBook(UUID id, Book updatedBook) {
        return bookRepository.findById(id).map(book -> {
            book.setTitle(updatedBook.getTitle());
            book.setIsbn(updatedBook.getIsbn());
            book.setBorrowed(updatedBook.isBorrowed());
            book.setImageUrl(updatedBook.getImageUrl());
            book.setPublishedDate(updatedBook.getPublishedDate());
            book.setDescription(updatedBook.getDescription());
            book.setAuthor(updatedBook.getAuthor());
            book.setCategory(updatedBook.getCategory());
            return bookRepository.save(book);
        }).orElseThrow(() -> new IllegalArgumentException("Book with ID " + id + " not found."));
    }

    // Delete a book by its ID
    public void deleteBook(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new IllegalArgumentException("Book with ID " + id + " not found.");
        }
        bookRepository.deleteById(id);
    }

    // Get all books
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Async
    public void uploadBookImage(UUID bookId, MultipartFile file) throws IOException {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new IllegalArgumentException("Book not found"));

        String fileName = bookId.toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR + fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, file.getBytes());

        // Update book with image URL
        book.setImageUrl(filePath.toString());
        bookRepository.save(book);
    }

    public Page<Book> getPaginatedBooks(int page) {
        Pageable pageable = PageRequest.of(page - 1, 20); // 20 books per page
        return bookRepository.findAll(pageable);
    }
}
