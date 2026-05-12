package com.unibuc.library.service;

import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceNotFoundException;
import com.unibuc.library.model.Book;
import com.unibuc.library.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book createBook(Book book) {
        // Check if ISBN already exists
        bookRepository.findByIsbn(book.getIsbn())
                .ifPresent(existingBook -> {
                    throw new DuplicateResourceException(
                            "A book with ISBN '" + book.getIsbn() + "' already exists"
                    );
                });

        return bookRepository.save(book);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAllWithAuthorsAndCategory();
    }

    public Book getBookById(Long id) {
        return bookRepository.findByIdWithAuthorsAndCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + id
                ));
    }

    public void deleteBook(Long id) {
        Book book = getBookById(id);
        bookRepository.delete(book);
    }

    public List<Book> searchBooksByTitle(String keyword) {
        return bookRepository.findAllWithAuthorsAndCategory().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Book> searchBooks(String title, String author, String category) {
        return bookRepository.findAllWithAuthorsAndCategory().stream()
                .filter(book -> title == null ||
                        (book.getTitle().toLowerCase().contains(title.toLowerCase())))
                .filter(book -> author == null ||
                        (book.getAuthors() != null &&
                                book.getAuthors().stream()
                                        .anyMatch(a -> a.getName().toLowerCase().contains(author.toLowerCase()))))
                .filter(book -> category == null ||
                        (book.getCategory() != null &&
                                book.getCategory().getName().toLowerCase().contains(category.toLowerCase())))
                .collect(Collectors.toList());
    }
}
