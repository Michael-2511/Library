# Library Management System

## Project Description

A library management system built with **Java 21** and **Spring Boot 4**. The application enables management of books, authors, categories, users, loans, and reservations, featuring a Thymeleaf web interface and a REST API documented with OpenAPI/Swagger.

---

## Architecture

```
com.unibuc.library
├── config/          → Configuration (DataLoader, OpenAPI)
├── controller/      → REST controllers (/rest/*) + Web MVC (Thymeleaf)
├── dto/             → Form DTOs (BookForm, UserForm)
├── exception/       → Custom exceptions + GlobalExceptionHandler
├── model/           → JPA Entities
├── repository/      → Spring Data JPA Repositories
├── security/        → Spring Security (config + UserDetailsService)
└── service/         → Business logic layer
```

---

## Setup & Running

### Prerequisites
- Java 21+
- PostgreSQL 15+ (for the `dev` profile)
- Maven (or use the included wrapper `mvnw.cmd`)

### Database Setup (dev)
```sql
CREATE DATABASE library_db;
-- User: postgres / Password: post-admin
```

### Run
```bash
.\mvnw.cmd spring-boot:run
```
The application starts on `http://localhost:8080`

### Default Credentials
| Role      | Email             | Password    |
|-----------|-------------------|-------------|
| ADMIN     | admin@admin       | adminadmin  |
| LIBRARIAN | librarian@local   | lib12345    |
| MEMBER    | member@local      | member123   |

---

## Part I — Mandatory Requirements

---

### 1. Data Model

#### Entities (6 entities + 2 enums)

| Entity          | Table            | Description                        |
|-----------------|------------------|------------------------------------|
| `Book`          | `books`          | Book with unique ISBN, copies, etc.|
| `Author`        | `authors`        | Author with unique name            |
| `Category`      | `categories`     | Category with unique name          |
| `User`          | `users`          | User with email, role, password    |
| `UserProfile`   | `user_profiles`  | Extended profile (phone, address)  |
| `Loan`          | `loans`          | Loan with dates and state          |
| `Reservation`   | `reservations`   | Reservation with status            |

#### Enums
- `UserRole` — `ADMIN`, `LIBRARIAN`, `MEMBER`
- `ReservationStatus` — `ACTIVE`, `CANCELLED`, `COMPLETED`

#### Relationships

| Relationship Type | Example                                      |
|-------------------|----------------------------------------------|
| `@OneToOne`       | `User` ↔ `UserProfile`                      |
| `@ManyToOne`      | `Loan` → `User`, `Loan` → `Book`            |
| `@ManyToOne`      | `Reservation` → `User`, `Reservation` → `Book` |
| `@ManyToOne`      | `Book` → `Category`                         |
| `@ManyToMany`     | `Book` ↔ `Author` (join table `book_authors`) |

#### ER Diagram

![ER Diagram](src\main\resources\static\awbd-erd-diagram.svg)

**OneToOne justification (User ↔ UserProfile):** Authentication data (User) is separated from optional contact information (UserProfile) for clarity and extensibility. User holds the data required for login and management, while UserProfile holds supplementary details like phone number and address.

---

### 2. Complete CRUD Operations

#### Repository Pattern — Spring Data JPA
Each entity has a dedicated repository extending `JpaRepository`:
- `BookRepository` — custom queries (`findByIsbn`, `findAllWithAuthorsAndCategory`, `findByIdWithAuthorsAndCategory`)
- `AuthorRepository` — `findByName`, `findAllWithBooks`, `findByIdWithBooks`
- `CategoryRepository` — `findByName`
- `UserRepository` — `findByEmail`
- `LoanRepository` — `findByReturnDateIsNull`, `countByUserIdAndReturnDateIsNull`, `existsByBookId`
- `ReservationRepository` — `findByUserId`, `existsByUserId`

#### Service Layer
Each entity has a dedicated service with business logic:
- `BookService` — CRUD + advanced search + unique ISBN check + deletion protection if loans exist
- `AuthorService` — CRUD + unique name check + deletion protection if books are associated
- `CategoryService` — CRUD + unique name check + deletion protection if books exist
- `UserService` — CRUD + BCrypt password encoding + unique email check + deletion protection
- `LoanService` — borrow/return + stock verification + user limit check + overdue detection
- `ReservationService` — create/cancel/complete + duplicate check + conversion to loan

#### Exception Handling
- `ResourceNotFoundException` → HTTP 404
- `DuplicateResourceException` → HTTP 409
- `ResourceInUseException` → HTTP 409
- `GlobalExceptionHandler` (`@ControllerAdvice`) — maps exceptions to Thymeleaf error pages

#### Controllers
Each entity has:
- **REST Controller** (`/rest/*`) — JSON API with OpenAPI annotations
- **Web Controller** — Thymeleaf interface with forms

---

### 3. Multi-Environment Configuration

#### Spring Profiles: `dev` and `test`

| Profile | Database                        | DDL Strategy    | File                      |
|---------|---------------------------------|-----------------|---------------------------|
| `dev`   | PostgreSQL (`localhost:5432`)    | `update`        | `application-dev.yml`     |
| `test`  | H2 in-memory (`library_test_db`)| `create-drop`   | `application-test.yml`    |

#### Global Configuration (`application.yml`)
```yaml
spring:
  application:
    name: library
  profiles:
    active: dev

library:
  pagination:
    page-size: 5
```

#### Test Profile (`application-test.yml`)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:library_test_db
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
  h2:
    console:
      enabled: true
```

---

### 4. Testing

#### Unit Tests — JUnit 5 + Mockito
Test files for each service:
- `BookServiceTest.java`
- `AuthorServiceTest.java`
- `CategoryServiceTest.java`
- `LoanServiceTest.java`
- `ReservationServiceTest.java`
- `UserServiceTest.java`

**Pattern used:**
```java
@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @Mock private BookRepository bookRepository;
    @InjectMocks private BookService bookService;

    @Test void createBook_Success() {
        when(bookRepository.findByIsbn(...)).thenReturn(Optional.empty());
        when(bookRepository.save(any())).thenReturn(book);
        // ...
        verify(bookRepository).save(book);
    }
}
```

#### Integration Tests — 3 End-to-End Scenarios (`LibraryE2ETest.java`)

| # | Scenario                           | Description                                                                                  |
|---|------------------------------------|----------------------------------------------------------------------------------------------|
| 1 | Book borrowing flow                | Create user → create book → borrow → verify stock decremented                               |
| 2 | Reservation flow (stock = 0)       | U1 borrows → U2 reserves → U1 returns → U2 completes reservation → verify stock = 0        |
| 3 | Overdue loan detection flow        | Create loan with dueDate in the past → verify it appears in overdue list                     |

**Test configuration:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
```

#### Running Tests
```bash
.\mvnw.cmd test
```

#### Coverage (JaCoCo)
```bash
.\mvnw.cmd test jacoco:report
# Report at: target/site/jacoco/index.html
```

---

### 5. Views and Validation

#### Frontend: Thymeleaf + Bootstrap 5
Pages for all CRUD operations:
- `/books` — list, detail, create/edit form
- `/authors` — list, detail, create/edit form
- `/categories` — list, detail, create/edit form
- `/users` — list, create/edit form (ADMIN only)
- `/loans` — list (active/overdue/all), new borrow form, return
- `/reservations` — list, create, cancel, complete

#### Server-Side Validation — Bean Validation
```java
// BookForm.java
@NotBlank(message = "Title is required")
private String title;

@Pattern(regexp = "^[\\d\\-]{10,17}$", message = "ISBN must be 10–17 digits/hyphens")
private String isbn;

@NotNull @Min(value = 1, message = "Total copies must be at least 1")
private Integer totalCopies;

@NotNull @Min(value = 0, message = "Available copies cannot be negative")
private Integer availableCopies;
```

Other annotations used: `@Valid`, `@NotNull`, `@Email`, `@Min`, `@NotBlank`

#### Client-Side Validation
- `required` — mandatory fields
- `min` — minimum values
- `pattern` — ISBN format
- `type="email"` — native HTML5 email validation

#### Custom Error Pages
- `error/404.html` — Resource Not Found
- `error/500.html` — Internal Server Error
- `error/error.html` — Generic errors (Duplicate, ResourceInUse)

---

### 6. Logging

#### Framework: SLF4J + Logback

#### Configuration (`logback-spring.xml`)

| Appender     | Destination          | Min Level | File                 |
|--------------|----------------------|-----------|----------------------|
| `CONSOLE`    | stdout               | INFO      | —                    |
| `FILE`       | `library.log`        | INFO      | Rolling (10MB/7 days)|
| `ERROR_FILE` | `library-error.log`  | ERROR     | Rolling (10MB/7 days)|

#### Configured Levels
```xml
<logger name="com.unibuc.library" level="DEBUG"/>
<logger name="org.springframework" level="WARN"/>
<logger name="org.hibernate" level="WARN"/>
```

#### Usage in Code
```java
log.info("Creating book with ISBN '{}' and title '{}'", book.getIsbn(), book.getTitle());
log.debug("Fetching books page {} with size {}", pageable.getPageNumber(), pageable.getPageSize());
log.warn("Cannot delete book '{}' (id={}) because it has associated loans", book.getTitle(), id);
```

---

### 7. Pagination and Sorting

#### Entities with Pagination
- **Books** — `BookWebController` + `BookService.getBooksPage(Pageable)`
- **Authors** — `AuthorWebController` + `AuthorService.getAuthorsPage(Pageable)`
- **Loans** — filter by active/overdue/all

#### Sort Criteria

| Entity  | Criterion 1 | Criterion 2        |
|---------|-------------|---------------------|
| Books   | `title`     | `isbn`, `totalCopies` |
| Authors | `name`      | `id`                |

#### Configurable Page Sizes
- Accepted values: **5**, **10**, **20**
- Default from `application.yml`: `library.pagination.page-size: 5`

#### Pagination UI
- Previous / Next buttons
- Page numbers (1, 2, 3...)
- Page size selector (5 / 10 / 20)
- Sort direction toggle (ASC / DESC)

---

### 8. Spring Security

#### Authentication
- **JDBC-based** — `CustomUserDetailsService` loads user from PostgreSQL via `UserRepository.findByEmail()`
- **Password encoding** — BCrypt (`BCryptPasswordEncoder`)
- **Custom login page** — `/login` with email + password
- **Remember me** — checkbox on login page
- **Functional logout** — POST to `/logout` with redirect to `/login?logout`

#### Roles (3)
| Role       | Permissions                                                                |
|------------|----------------------------------------------------------------------------|
| `ADMIN`    | Full access — including user management (`/users/**`)                      |
| `LIBRARIAN`| CRUD on Books, Authors, Categories + Loans + Reservations                  |
| `MEMBER`   | View books/authors/categories + Loans + Reservations (own)                 |

#### Endpoint Protection
```java
.requestMatchers("/users/**").hasRole("ADMIN")
.requestMatchers("/books/new", "/books/*/edit", "/books/*/delete").hasAnyRole("LIBRARIAN", "ADMIN")
.requestMatchers("/authors/new", "/authors/*/edit", "/authors/*/delete").hasAnyRole("LIBRARIAN", "ADMIN")
.requestMatchers("/categories/new", "/categories/*/edit", "/categories/*/delete").hasAnyRole("LIBRARIAN", "ADMIN")
.anyRequest().authenticated()
```

#### CSRF Protection
- Active on all forms (`th:name="${_csrf.parameterName}"`)
- Disabled only for H2 Console (development)

#### Method Security
- `@EnableMethodSecurity` enabled
- UI hides unauthorized elements with `sec:authorize="hasRole('ADMIN')"`

---

## Part II — Optional Requirements

---

### 11. CI/CD Pipeline

#### Technology: GitHub Actions

A continuous integration pipeline is configured in `.github/workflows/ci.yml` and runs automatically on every push to `main`/`dev` and on pull requests.

#### Pipeline Steps

| Step                        | Description                                      |
|-----------------------------|--------------------------------------------------|
| Checkout code               | Pulls the latest source from the repository      |
| Set up JDK 21              | Installs Temurin JDK 21 with Maven caching       |
| Run tests (H2 profile)     | Executes all unit + integration tests            |
| Generate JaCoCo report     | Produces code coverage report                    |
| Upload coverage artifact   | Saves JaCoCo HTML report as downloadable artifact|

#### Triggers
- **Push** to `main` or `dev` branches
- **Pull requests** targeting `main`

#### Services
- PostgreSQL 15 service container (available if needed for dev-profile tests)
- Tests run against H2 in-memory DB (test profile) for speed and isolation

---

### 12. AI Agents — Development

#### Tools Used

Kiro AI was used for pair programming and code generation.

#### Usage and Benefits

**Pair Programming & Code Generation**
- Kiro was used as the primary AI pair-programming assistant throughout the entire development process
- Assisted with implementing CRUD operations, service layer logic, security configuration, and Thymeleaf views
- Generated complete `DataLoader` seed data with 30 books, 25 authors, and 6 categories in a single session
- Helped troubleshoot runtime errors (e.g., duplicate key constraint violations) by analyzing error logs and suggesting fixes

**Documentation Generation**
- This README was generated with Kiro's assistance, analyzing the existing codebase structure and mapping it to project requirements
- Kiro read all model classes, services, controllers, configurations, and test files to produce accurate documentation

**Key Benefits**
- Significantly reduced boilerplate coding time (entity definitions, CRUD controllers, form DTOs)
- Faster debugging through log analysis and root-cause identification
- Consistent code quality — Kiro follows the existing project conventions (naming, structure, patterns)
- Documentation stays in sync with actual implementation since it's generated from the codebase

---

## API Documentation (Swagger)

Available at: `http://localhost:8080/swagger-ui.html`

REST endpoints under `/rest/`:
- `/rest/books` — CRUD + search
- `/rest/authors` — CRUD
- `/rest/categories` — CRUD
- `/rest/users` — CRUD
- `/rest/loans` — borrow, return, overdue
- `/rest/reservations` — create, cancel, complete

---

## Tech Stack

| Technology                 | Version   | Purpose                       |
|----------------------------|-----------|-------------------------------|
| Java                       | 21        | Main language                 |
| Spring Boot                | 4.0.1     | Application framework         |
| Spring Data JPA / Hibernate| —         | ORM persistence               |
| Spring Security            | —         | Authentication & authorization|
| Thymeleaf                  | —         | Template engine (views)       |
| PostgreSQL                 | 15+       | Database (production/dev)     |
| H2                         | —         | Database (tests)              |
| JUnit 5 + Mockito          | —         | Unit testing                  |
| JaCoCo                     | 0.8.11    | Code coverage                 |
| Logback (SLF4J)            | —         | Logging                       |
| SpringDoc OpenAPI          | 2.8.14    | API documentation (Swagger)   |
| Bootstrap                  | 5.3       | Frontend styling              |

---

## Deployment

### Live Application

**URL:** [http://awbd-library-1.us-east-1.elasticbeanstalk.com/](http://awbd-library-1.us-east-1.elasticbeanstalk.com/)

### Platform: AWS Elastic Beanstalk

The application is deployed on AWS using the following services:

| AWS Service          | Purpose                                      |
|----------------------|----------------------------------------------|
| Elastic Beanstalk    | Application hosting (Java 21 platform)       |
| RDS PostgreSQL       | Managed database (db.t3.micro, free tier)    |
| S3                   | Artifact storage for deployment packages     |
| Load Balancer        | Traffic routing to application instances     |

### Deployment Architecture

```
GitHub (push to main)
    │
    ▼
GitHub Actions (CI/CD)
    │  ── Run tests
    │  ── Build JAR
    │  ── Deploy to AWS
    ▼
AWS Elastic Beanstalk
    │
    ├── EC2 Instance (Java 21 + nginx)
    │       └── library-0.0.1-SNAPSHOT.jar
    │
    └── RDS PostgreSQL
            └── library_db (persistent data)
```

### Environment Variables

The following environment variables are configured in Elastic Beanstalk:

| Variable                    | Description                          |
|-----------------------------|--------------------------------------|
| `SPRING_PROFILES_ACTIVE`    | Active Spring profile (`dev`)        |
| `SPRING_DATASOURCE_URL`     | JDBC connection string to RDS        |
| `SPRING_DATASOURCE_USERNAME`| Database username                    |
| `SPRING_DATASOURCE_PASSWORD`| Database password                    |
| `SERVER_PORT`               | Application port (`5000`)            |

### Continuous Deployment

Deployment is automated via GitHub Actions. On every push to `main`:
1. Tests run with H2 (test profile)
2. JaCoCo coverage report is generated
3. JAR is built with `mvnw package`
4. JAR is deployed to Elastic Beanstalk using `einaregilsson/beanstalk-deploy`

The environment updates automatically within ~2 minutes of a successful push.

---

## Contributors

- **Răican Mihai** — Full development 
