package bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import bookstore.models.Book;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    // Find books by author ID
    List<Book> findByAuthor_Id(UUID authorId);

    // Find books by category ID
    List<Book> findByCategory_Id(UUID categoryId);

    // Find books by title containing a keyword (for search functionality)
    List<Book> findByTitleContaining(String keyword);

    // Find books by ISBN
    Book findByIsbn(String isbn);
    boolean existsByTitle(String title);
}
