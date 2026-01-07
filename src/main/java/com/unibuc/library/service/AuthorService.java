package com.unibuc.library.service;

import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceNotFoundException;
import com.unibuc.library.model.Author;
import com.unibuc.library.repository.AuthorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public Author createAuthor(Author author) {

        authorRepository.findByName(author.getName())
                .ifPresent(existingAuthor -> {
                    throw new DuplicateResourceException(
                            "Author with name '" + author.getName() + "' already exists"
                    );
                });

        return authorRepository.save(author);
    }

    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    public Author getAuthorById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Author not found with id: " + id
                ));
    }
}
