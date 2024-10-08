package bookstore.models;

import jakarta.persistence.*;
import java.util.UUID;
import java.util.List;

@Entity
@Table(name = "authors")
public class Author {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private UUID id;
    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "author")
    private List<Book> books;

    public Author() {}

    // getters and setters
}
