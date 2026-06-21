package com.unibuc.library.config;

import com.unibuc.library.model.*;
import com.unibuc.library.repository.AuthorRepository;
import com.unibuc.library.repository.BookRepository;
import com.unibuc.library.repository.CategoryRepository;
import com.unibuc.library.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    @Order(1)
    public CommandLineRunner loadInitialUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User librarian = new User();
                librarian.setName("Head Librarian");
                librarian.setEmail("librarian@local");
                librarian.setRole(UserRole.LIBRARIAN);
                librarian.setMaxBorrowLimit(20);
                librarian.setPassword(passwordEncoder.encode("lib12345"));
                librarian.setEnabled(true);

                User member = new User();
                member.setName("Demo Member");
                member.setEmail("member@local");
                member.setRole(UserRole.MEMBER);
                member.setMaxBorrowLimit(5);
                member.setPassword(passwordEncoder.encode("member123"));
                member.setEnabled(true);
                User realAdmin = new User();
                realAdmin.setName("Site Admin");
                realAdmin.setEmail("admin@admin");
                realAdmin.setRole(UserRole.ADMIN);
                realAdmin.setMaxBorrowLimit(99);
                realAdmin.setPassword(passwordEncoder.encode("adminadmin"));
                realAdmin.setEnabled(true);
                userRepository.save(librarian);
                userRepository.save(member);
                userRepository.save(realAdmin);

                log.info("=== INITIAL CREDENTIALS ===");
                log.info("ADMIN: admin@admin / adminadmin");
                log.info("LIBRARIAN: librarian@local / lib12345");
                log.info("MEMBER: member@local / member123");
                log.info("==========================");
            }
        };
    }

    @Bean
    @Order(2)
    public CommandLineRunner loadInitialBooks(BookRepository bookRepository,
                                             AuthorRepository authorRepository,
                                             CategoryRepository categoryRepository) {
        return args -> {
            if (bookRepository.count() > 21) {
                log.info("Books already exist — skipping seed data.");
                return;
            }

            // --- Categories ---
            Category fiction = categoryRepository.findByName("Fiction")
                    .orElseGet(() -> categoryRepository.save(new Category("Fiction")));
            Category science = categoryRepository.findByName("Science")
                    .orElseGet(() -> categoryRepository.save(new Category("Science")));
            Category history = categoryRepository.findByName("History")
                    .orElseGet(() -> categoryRepository.save(new Category("History")));
            Category philosophy = categoryRepository.findByName("Philosophy")
                    .orElseGet(() -> categoryRepository.save(new Category("Philosophy")));
            Category technology = categoryRepository.findByName("Technology")
                    .orElseGet(() -> categoryRepository.save(new Category("Technology")));
            Category romanianLit = categoryRepository.findByName("Romanian Literature")
                    .orElseGet(() -> categoryRepository.save(new Category("Romanian Literature")));

            // --- Authors (international) ---
            Author orwell = getOrCreateAuthor(authorRepository, "George Orwell");
            Author tolkien = getOrCreateAuthor(authorRepository, "J.R.R. Tolkien");
            Author asimov = getOrCreateAuthor(authorRepository, "Isaac Asimov");
            Author hawking = getOrCreateAuthor(authorRepository, "Stephen Hawking");
            Author dostoevsky = getOrCreateAuthor(authorRepository, "Fyodor Dostoevsky");
            Author camus = getOrCreateAuthor(authorRepository, "Albert Camus");
            Author hemingway = getOrCreateAuthor(authorRepository, "Ernest Hemingway");
            Author kafka = getOrCreateAuthor(authorRepository, "Franz Kafka");
            Author huxley = getOrCreateAuthor(authorRepository, "Aldous Huxley");
            Author bradbury = getOrCreateAuthor(authorRepository, "Ray Bradbury");
            Author harari = getOrCreateAuthor(authorRepository, "Yuval Noah Harari");
            Author knuth = getOrCreateAuthor(authorRepository, "Donald Knuth");
            Author austen = getOrCreateAuthor(authorRepository, "Jane Austen");
            Author marquez = getOrCreateAuthor(authorRepository, "Gabriel Garcia Marquez");
            Author plato = getOrCreateAuthor(authorRepository, "Plato");

            // --- Authors (Romanian) ---
            Author calinescu = getOrCreateAuthor(authorRepository, "George Calinescu");
            Author rebreanu = getOrCreateAuthor(authorRepository, "Liviu Rebreanu");
            Author eminescu = getOrCreateAuthor(authorRepository, "Mihai Eminescu");
            Author caragiale = getOrCreateAuthor(authorRepository, "Ion Luca Caragiale");
            Author eliade = getOrCreateAuthor(authorRepository, "Mircea Eliade");
            Author sadoveanu = getOrCreateAuthor(authorRepository, "Mihail Sadoveanu");
            Author slavici = getOrCreateAuthor(authorRepository, "Ioan Slavici");
            Author preda = getOrCreateAuthor(authorRepository, "Marin Preda");
            Author creanga = getOrCreateAuthor(authorRepository, "Ion Creanga");
            Author petrescu = getOrCreateAuthor(authorRepository, "Camil Petrescu");

            // --- Books (20) ---
            createBook(bookRepository, "1984", "978-0-451-52493-5", 5, 3, fiction, Set.of(orwell));
            createBook(bookRepository, "Animal Farm", "978-0-451-52634-2", 4, 4, fiction, Set.of(orwell));
            createBook(bookRepository, "The Lord of the Rings", "978-0-618-64015-7", 3, 1, fiction, Set.of(tolkien));
            createBook(bookRepository, "The Hobbit", "978-0-547-92822-7", 4, 2, fiction, Set.of(tolkien));
            createBook(bookRepository, "Foundation", "978-0-553-29335-7", 3, 3, science, Set.of(asimov));
            createBook(bookRepository, "I, Robot", "978-0-553-29438-5", 2, 1, science, Set.of(asimov));
            createBook(bookRepository, "A Brief History of Time", "978-0-553-38016-3", 4, 2, science, Set.of(hawking));
            createBook(bookRepository, "Crime and Punishment", "978-0-14-044913-6", 3, 3, fiction, Set.of(dostoevsky));
            createBook(bookRepository, "The Brothers Karamazov", "978-0-374-52837-9", 2, 1, fiction, Set.of(dostoevsky));
            createBook(bookRepository, "The Stranger", "978-0-679-72020-1", 3, 2, philosophy, Set.of(camus));
            createBook(bookRepository, "The Old Man and the Sea", "978-0-684-80122-3", 3, 3, fiction, Set.of(hemingway));
            createBook(bookRepository, "The Trial", "978-0-805-20999-0", 2, 2, fiction, Set.of(kafka));
            createBook(bookRepository, "The Metamorphosis", "978-0-553-21369-9", 3, 1, fiction, Set.of(kafka));
            createBook(bookRepository, "Brave New World", "978-0-06-085052-4", 4, 3, fiction, Set.of(huxley));
            createBook(bookRepository, "Fahrenheit 451", "978-1-451-67331-9", 3, 2, fiction, Set.of(bradbury));
            createBook(bookRepository, "Sapiens", "978-0-06-231609-7", 5, 4, history, Set.of(harari));
            createBook(bookRepository, "The Art of Computer Programming", "978-0-201-89683-1", 2, 1, technology, Set.of(knuth));
            createBook(bookRepository, "Pride and Prejudice", "978-0-14-143951-8", 4, 4, fiction, Set.of(austen));
            createBook(bookRepository, "One Hundred Years of Solitude", "978-0-06-088328-7", 3, 2, fiction, Set.of(marquez));
            createBook(bookRepository, "The Republic", "978-0-14-044914-3", 2, 2, philosophy, Set.of(plato));

            // --- Romanian Books (10) ---
            createBook(bookRepository, "Enigma Otiliei", "978-973-23-0845-3", 4, 3, romanianLit, Set.of(calinescu));
            createBook(bookRepository, "Ion", "978-973-23-0712-8", 3, 2, romanianLit, Set.of(rebreanu));
            createBook(bookRepository, "Padurea Spanzuratilor", "978-973-23-0713-5", 3, 3, romanianLit, Set.of(rebreanu));
            createBook(bookRepository, "Luceafarul", "978-973-23-0901-6", 5, 4, romanianLit, Set.of(eminescu));
            createBook(bookRepository, "O Scrisoare Pierduta", "978-973-23-0654-1", 4, 3, romanianLit, Set.of(caragiale));
            createBook(bookRepository, "Maitreyi", "978-973-23-0778-4", 3, 2, romanianLit, Set.of(eliade));
            createBook(bookRepository, "Baltagul", "978-973-23-0623-7", 4, 4, romanianLit, Set.of(sadoveanu));
            createBook(bookRepository, "Moara cu Noroc", "978-973-23-0567-4", 3, 2, romanianLit, Set.of(slavici));
            createBook(bookRepository, "Morometii", "978-973-23-0834-7", 3, 1, romanianLit, Set.of(preda));
            createBook(bookRepository, "Ultima Noapte de Dragoste, Intaia Noapte de Razboi", "978-973-23-0892-7", 3, 2, romanianLit, Set.of(petrescu));

            log.info("=== Seeded 30 books with authors and categories ===");
        };
    }

    private Author getOrCreateAuthor(AuthorRepository repo, String name) {
        return repo.findByName(name).orElseGet(() -> repo.save(new Author(name)));
    }

    private void createBook(BookRepository repo, String title, String isbn,
                            int total, int available, Category category, Set<Author> authors) {
        if (repo.findByIsbn(isbn).isPresent()) {
            return; // already exists, skip
        }
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setTotalCopies(total);
        book.setAvailableCopies(available);
        book.setCategory(category);
        book.setAuthors(authors);
        repo.save(book);
    }
}


