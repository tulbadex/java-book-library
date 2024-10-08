package bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import bookstore.models.Book;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
}