package bookstore.service;

import bookstore.dto.AuthorDTO;
import bookstore.dto.UpdateAuthorDTO;
import bookstore.models.Author;
import bookstore.repository.AuthorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private static final String UPLOAD_DIR = "uploads/authors";

    @Autowired
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    // Cache paginated authors by page number
    @Cacheable(value = "authors", key = "'page_' + #page") // Cache paginated results
    public Page<Author> getPaginatedAuthors(int page) {
        int pageSize = 20; // Define page size
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        return authorRepository.findAll(pageable);
    }

    // Cache the list of all authors
    @Cacheable(value = "authors", key = "'all'")
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    // Cache individual author by ID
    @Cacheable(value = "authors", key = "#id")
    public Author findAuthorById(UUID id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid author ID"));
    }

    // Clear all authors cache on create
    @Transactional
    @CacheEvict(value = "authors", allEntries = true) // Clear paginated caches
    @CachePut(value = "authors", key = "#result.id") 
    public Author createAuthor(@Valid AuthorDTO authorDTO, MultipartFile file) {
        if (authorRepository.existsByEmail(authorDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists!");
        }
        try {
            Author author = new Author();
            populateAuthorFields(author, authorDTO);
            // author.setName(authorDTO.getName());
            // author.setEmail(authorDTO.getEmail());
            // author.setBiography(authorDTO.getBiography());
            // author.setGender(authorDTO.getGender());

            Author savedAuthor = authorRepository.save(author);
            if (file != null && !file.isEmpty()) {
                this.uploadAuthorImage(savedAuthor.getId(), file);
            }
            return savedAuthor;
        } catch (DataAccessException | IOException e) {
            throw new RuntimeException("Error creating author: " + e.getMessage());
        }
    }

    @Async
    public void uploadAuthorImage(UUID authorId, MultipartFile file) throws IOException {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        String fileName = authorId.toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, file.getBytes());

        // Update author with image URL
        // author.setImageUrl(fileName);
        // Set the relative URL for the image
        author.setImageUrl("/" + UPLOAD_DIR + "/" + fileName);
        authorRepository.save(author);
    }

    // Update author and refresh cache
    @Transactional
    @CacheEvict(value = "authors", allEntries = true) // Clear related caches
    @CachePut(value = "authors", key = "#id")
    public Author updateAuthor(UUID id, @Valid UpdateAuthorDTO authorDTO, MultipartFile file) {
        Author author = findAuthorById(id);

        try {
            author.setId(authorDTO.getId());
            author.setName(authorDTO.getName());
            author.setEmail(authorDTO.getEmail());
            author.setBiography(authorDTO.getBiography());
            author.setGender(authorDTO.getGender());
            if (authorDTO.getImageUrl() != null) {
                author.setImageUrl(authorDTO.getImageUrl());
            }

            if (file != null && !file.isEmpty()) {
                this.uploadAuthorImage(id, file);
            }

            return authorRepository.save(author);
        } catch (DataAccessException | IOException e) {
            throw new RuntimeException("Error updating author: " + e.getMessage());
        }
    }

    // Clear specific author from cache and evict paginated/all cache
    @Transactional
    @CacheEvict(value = "authors", allEntries = true)
    public void deleteAuthor(UUID id) {
        Author author = findAuthorById(id);

        try {
            authorRepository.delete(author);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error deleting author: " + e.getMessage());
        }
    }

    private void populateAuthorFields(Author author, AuthorDTO authorDTO) {
        author.setName(authorDTO.getName());
        author.setEmail(authorDTO.getEmail());
        author.setBiography(authorDTO.getBiography());
        author.setGender(authorDTO.getGender());
        if (authorDTO.getImageUrl() != null) {
            author.setImageUrl(authorDTO.getImageUrl());
        }
    }
}