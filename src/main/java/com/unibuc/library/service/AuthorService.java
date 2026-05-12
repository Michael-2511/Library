package com.unibuc.library.service;

import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceInUseException;
import com.unibuc.library.exception.ResourceNotFoundException;
import com.unibuc.library.model.Author;
import com.unibuc.library.repository.AuthorRepository;
import com.unibuc.library.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
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

    public Author updateAuthor(Long id, Author author) {
        Author existingAuthor = getAuthorById(id);

        authorRepository.findByName(author.getName())
                .ifPresent(foundAuthor -> {
                    if (!foundAuthor.getId().equals(id)) {
                        throw new DuplicateResourceException(
                                "Author with name '" + author.getName() + "' already exists"
                        );
                    }
                });

        existingAuthor.setName(author.getName());
        return authorRepository.save(existingAuthor);
    }

    public void deleteAuthor(Long id) {
        Author existingAuthor = getAuthorById(id);

        boolean authorIsUsed = bookRepository.findAll().stream()
                .filter(book -> book.getAuthors() != null)
                .flatMap(book -> book.getAuthors().stream())
                .anyMatch(author -> author.getId().equals(id));

        if (authorIsUsed) {
            throw new ResourceInUseException(
                    "Author cannot be deleted because it is associated with one or more books"
            );
        }

        authorRepository.delete(existingAuthor);
    }
}
