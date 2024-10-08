package bookstore.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String title;
    private String isbn;
    private boolean isBorrowed;

    @ManyToOne
    private Author author;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public boolean isBorrowed() {
        return isBorrowed;
    }

    public void setBorrowed(boolean isBorrowed) {
        this.isBorrowed = isBorrowed;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }
}
