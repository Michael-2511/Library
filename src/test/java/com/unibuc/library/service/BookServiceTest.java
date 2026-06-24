package com.unibuc.library.service;

import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceNotFoundException;
import com.unibuc.library.model.*;
import com.unibuc.library.repository.BookRepository;
import com.unibuc.library.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private Author author;
    private Category category;

    @BeforeEach
    void setUp() {
        author = new Author();
        author.setId(1L);
        author.setName("George R.R. Martin");

        category = new Category();
        category.setId(1L);
        category.setName("Fantasy");

        book = new Book();
        book.setId(1L);
        book.setTitle("A Game of Thrones");
        book.setIsbn("9780553573404");
        book.setTotalCopies(10);
        book.setAvailableCopies(10);
        
        Set<Author> authors = new HashSet<>();
        authors.add(author);
        book.setAuthors(authors);
        
        book.setCategory(category);
    }

    @Test
    void createBook_Success() {
        // Arrange
        when(bookRepository.findByIsbn(book.getIsbn())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book result = bookService.createBook(book);

        // Assert
        assertNotNull(result);
        assertEquals(book.getId(), result.getId());
        assertEquals(book.getTitle(), result.getTitle());
        assertEquals(book.getIsbn(), result.getIsbn());
        verify(bookRepository).findByIsbn(book.getIsbn());
        verify(bookRepository).save(book);
    }

    @Test
    void createBook_DuplicateISBN_ThrowsException() {
        // Arrange
        when(bookRepository.findByIsbn(book.getIsbn())).thenReturn(Optional.of(book));

        // Act & Assert
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> bookService.createBook(book));

        assertEquals("A book with ISBN '9780553573404' already exists", exception.getMessage());
        verify(bookRepository).findByIsbn(book.getIsbn());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void getAllBooks_Success() {
        // Arrange
        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("The Hunger Games");
        book2.setIsbn("9780439023528");

        List<Book> books = Arrays.asList(book, book2);
        when(bookRepository.findAllWithAuthorsAndCategory()).thenReturn(books);

        // Act
        List<Book> result = bookService.getAllBooks();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("A Game of Thrones", result.get(0).getTitle());
        assertEquals("The Hunger Games", result.get(1).getTitle());
        verify(bookRepository).findAllWithAuthorsAndCategory();
    }

    @Test
    void getBookById_Success() {
        // Arrange
        when(bookRepository.findByIdWithAuthorsAndCategory(1L)).thenReturn(Optional.of(book));

        // Act
        Book result = bookService.getBookById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("A Game of Thrones", result.getTitle());
        verify(bookRepository).findByIdWithAuthorsAndCategory(1L);
    }

    @Test
    void getBookById_NotFound_ThrowsException() {
        // Arrange
        when(bookRepository.findByIdWithAuthorsAndCategory(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookService.getBookById(999L));

        assertEquals("Book not found with id: 999", exception.getMessage());
        verify(bookRepository).findByIdWithAuthorsAndCategory(999L);
    }

    @Test
    void deleteBook_Success() {
        // Arrange
        when(bookRepository.findByIdWithAuthorsAndCategory(1L)).thenReturn(Optional.of(book));
        when(loanRepository.existsByBookId(1L)).thenReturn(false);
        doNothing().when(bookRepository).delete(book);

        // Act
        bookService.deleteBook(1L);

        // Assert
        verify(bookRepository).findByIdWithAuthorsAndCategory(1L);
        verify(loanRepository).existsByBookId(1L);
        verify(bookRepository).delete(book);
    }

    @Test
    void searchBooksByTitle_Success() {
        // Arrange
        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("The Hunger Games");

        List<Book> allBooks = Arrays.asList(book, book2);
        when(bookRepository.findAllWithAuthorsAndCategory()).thenReturn(allBooks);

        // Act
        List<Book> result = bookService.searchBooksByTitle("Game");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // Both contain "Game"
        verify(bookRepository).findAllWithAuthorsAndCategory();
    }

    @Test
    void searchBooksByTitle_NoMatch_ReturnsEmpty() {
        // Arrange
        when(bookRepository.findAllWithAuthorsAndCategory()).thenReturn(Arrays.asList(book));

        // Act
        List<Book> result = bookService.searchBooksByTitle("Nonexistent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookRepository).findAllWithAuthorsAndCategory();
    }

    @Test
    void searchBooks_AllCriteria_Success() {
        // Arrange
        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Catching Fire");
        
        Set<Author> authors2 = new HashSet<>();
        authors2.add(author);
        book2.setAuthors(authors2);
        
        book2.setCategory(category);

        List<Book> allBooks = Arrays.asList(book, book2);
        when(bookRepository.findAllWithAuthorsAndCategory()).thenReturn(allBooks);

        // Act
        List<Book> result = bookService.searchBooks("Game", "Martin", "Fantasy");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("A Game of Thrones", result.get(0).getTitle());
        verify(bookRepository).findAllWithAuthorsAndCategory();
    }

    @Test
    void searchBooks_PartialCriteria_Success() {
        // Arrange
        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("The Hunger Games");
        
        Set<Author> authors2 = new HashSet<>();
        authors2.add(author);
        book2.setAuthors(authors2);
        
        book2.setCategory(category);

        List<Book> allBooks = Arrays.asList(book, book2);
        when(bookRepository.findAllWithAuthorsAndCategory()).thenReturn(allBooks);

        // Act - Search only by author
        List<Book> result = bookService.searchBooks(null, "Martin", null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookRepository).findAllWithAuthorsAndCategory();
    }

    @Test
    void searchBooks_NoCriteria_ReturnsAll() {
        // Arrange
        List<Book> allBooks = Arrays.asList(book);
        when(bookRepository.findAllWithAuthorsAndCategory()).thenReturn(allBooks);

        // Act
        List<Book> result = bookService.searchBooks(null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookRepository).findAllWithAuthorsAndCategory();
    }

    @Test
    void searchBooks_CaseInsensitive_Success() {
        // Arrange
        when(bookRepository.findAllWithAuthorsAndCategory()).thenReturn(Arrays.asList(book));

        // Act - Different cases should still match
        List<Book> result1 = bookService.searchBooks("GAME", null, null);
        List<Book> result2 = bookService.searchBooks("game", null, null);
        List<Book> result3 = bookService.searchBooks("GaMe", null, null);

        // Assert
        assertEquals(1, result1.size());
        assertEquals(1, result2.size());
        assertEquals(1, result3.size());
        verify(bookRepository, times(3)).findAllWithAuthorsAndCategory();
    }
}
