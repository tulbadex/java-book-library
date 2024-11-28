package bookstore.service;

import bookstore.dto.AuthorDTO;
import bookstore.models.Author;
import bookstore.models.Book;
import bookstore.repository.AuthorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final static String UPLOAD_DIR = "uploads/authors/";

    @Autowired
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public Page<Author> getPaginatedAuthors(int page) {
        Pageable pageable = PageRequest.of(page - 1, 20, Sort.by("name").descending()); // Sort by name in descending order
        return authorRepository.findAll(pageable);
    }

    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    public int getTotalPages() {
        long totalAuthors = authorRepository.count();
        return (int) Math.ceil((double) totalAuthors / 20);
    }

    public Author findAuthorById(UUID id) {
        return authorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid author ID"));
    }

    @Transactional
    public Author createAuthor(@Valid AuthorDTO authorDTO, MultipartFile file) {
        try {
            Author author = new Author();
            author.setName(authorDTO.getName());
            author.setEmail(authorDTO.getEmail());
            author.setBiography(authorDTO.getBiography());

            Author savedAuthor = authorRepository.save(author);
            this.uploadAuthorImage(savedAuthor.getId(), file); // Upload image
            return savedAuthor;
        } catch (DataAccessException | IOException e) {
            throw new RuntimeException("Error creating author: " + e.getMessage());
        }
    }

    @Async
    public void uploadAuthorImage(UUID authorId, MultipartFile file) throws IOException {
        Author author = authorRepository.findById(authorId).orElseThrow(() -> new IllegalArgumentException("Author not found"));

        String fileName = authorId.toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, file.getBytes());

        // Update author with image URL
        author.setImageUrl(fileName); // Store only the file name or relative path
        authorRepository.save(author);
    }

    @Transactional
    public Author updateAuthor(UUID id, @Valid AuthorDTO authorDTO) {
        Author author = findAuthorById(id);

        try {
            author.setName(authorDTO.getName());
            author.setEmail(authorDTO.getEmail());
            author.setBiography(authorDTO.getBiography());
            author.setImageUrl(authorDTO.getImageUrl());

            return authorRepository.save(author);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error updating author: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteAuthor(UUID id) {
        Author author = findAuthorById(id);

        try {
            authorRepository.delete(author);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error deleting author: " + e.getMessage());
        }
    }
}
