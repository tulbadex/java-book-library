package bookstore.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bookstore.models.Book;
import bookstore.repository.BookRepository;

@Service
public class LibraryService {
    @Autowired
    private BookRepository bookRepository;

    /* public String borrowBook(Long bookId, Long userId) {
        Optional<Book> book = bookRepository.findById(bookId);
        if (book.isPresent() && !book.get().isBorrowed()) {
            book.get().setBorrowed(true);
            bookRepository.save(book.get());
            return "Book borrowed successfully";
        }
        return "Book not available";
    }

    public String returnBook(Long bookId) {
        Optional<Book> book = bookRepository.findById(bookId);
        if (book.isPresent() && book.get().isBorrowed()) {
            book.get().setBorrowed(false);
            bookRepository.save(book.get());
            return "Book returned successfully";
        }
        return "Book not borrowed";
    } */
}

