package com.example.demo.application;

import com.example.demo.controllers.domain.Model.BookSummary;
import com.example.demo.controllers.domain.Model.UserSummary;
import com.example.demo.controllers.domain.entity.Author;
import com.example.demo.controllers.domain.entity.Book;
import com.example.demo.controllers.domain.entity.Categories;
import com.example.demo.controllers.domain.entity.Review;
import com.example.demo.controllers.domain.repository.AuthorRepository;
import com.example.demo.controllers.domain.repository.BookRepository;
import com.example.demo.controllers.domain.repository.CategoriesRepository;
import com.example.demo.controllers.domain.repository.ReviewRepository;
import com.example.demo.controllers.dto.BookRequest;
import com.example.demo.controllers.exception.BookAlreadyExistsException;
import com.example.demo.controllers.exception.BookNotFoundException;
import com.example.demo.controllers.response.BookResponse;
import com.example.demo.controllers.response.LoanSummaryResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoriesRepository genreRepository;
    private final MongoTemplate mongoTemplate;
    private final CacheManager cacheManager;
    private final ReviewRepository reviewRepository;

    @Transactional
    public BookResponse createBook(BookRequest request) {
        cacheManager.getCache("books").clear();

        // Verificar si ya existe un libro con el mismo ISBN
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BookAlreadyExistsException("Ya existe un libro con el ISBN: " + request.getIsbn());
        }

        // Mapear BookRequest a la entidad Book
        Book book = Book.builder()
                .title(request.getTitle())
                .synopsis(request.getSynopsis())
                .categories(request.getCategories())
                .isbn(request.getIsbn())
                .author(request.getAuthor())
                .publisher(request.getPublisher())
                .publicationDate(request.getPublicationDate())
                .pageCount(request.getPageCount())
                .language(request.getLanguage())
                .coverImageUrl(request.getCoverImageUrl())
                .averageRating(request.getAverageRating())
                .ratingsCount(request.getRatingsCount())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Guardar el libro
        Book savedBook = bookRepository.save(book);
        log.info("Libro creado con ID: {}", savedBook.getId());

        // Crear o actualizar el autor con el libro nuevo
        updateAuthorWithNewBook(savedBook);

        // Crear o actualizar géneros con el nuevo libro
        updateGenresWithNewBook(savedBook);

        // Convertir a DTO y retornar
        return mapToBookResponse(savedBook);
    }

    @Transactional(readOnly = true)
    @Cacheable(value="books")
    public List<BookResponse> getBooks(Boolean available) {
        List<Book> books;
        if (available == null) {
            books = bookRepository.findAll();
        } else {
            books = bookRepository.findByAvailable(available);
        }
        return books.stream().map(this::mapToBookResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value="booksById")
    public BookResponse getBookById(String id) {
        return bookRepository.findById(id)
                .map(this::mapToBookResponse)
                .orElseThrow(() -> new BookNotFoundException("Libro no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getTopRated() {
        List<Book> books;
        books = bookRepository.findByAverageRating(5);
        return books.stream().map(this::mapToBookResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value="BooksBySearch")
    public List<BookResponse> searchBooks(String query) {
        // Búsqueda por título, autor o género
        Query searchQuery = new Query();
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("title").regex(query, "i"),
                Criteria.where("author").regex(query, "i"),
                Criteria.where("genres").in(query)
        );
        searchQuery.addCriteria(criteria);

        return mongoTemplate.find(searchQuery, Book.class)
                .stream()
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findBooksByGenre(String genre) {
        return bookRepository.findByGenre(genre)
                .stream()
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findBooksByGenreInMemory(String genre) {
        log.info("Buscando todos los libros y filtrando en memoria por el género: {}", genre);
        return bookRepository.findAll()
                .stream()
                .filter(book -> book.getCategories().contains(genre))
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteBook(String id) {
        cacheManager.getCache("books").clear();
        cacheManager.getCache("booksById").evict(id);
        cacheManager.getCache("BooksBySearch").evict(id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("No se puede eliminar. Libro no encontrado con ID: " + id));

        // Eliminar el libro del autor
        authorRepository.findByName(book.getAuthor()).ifPresent(author -> {
            author.getBooks().removeIf(summary -> summary.getBookId().equals(id));
            authorRepository.save(author);
        });

        // Eliminar el libro de los géneros
        book.getCategories().forEach(genreName ->
                genreRepository.findByName(genreName).ifPresent(genre -> {
                    genre.getBooks().removeIf(summary -> summary.getBookId().equals(id));
                    genreRepository.save(genre);
                })
        );

        bookRepository.delete(book);
        log.info("Libro eliminado con ID: {}", id);
    }

    @Transactional
    public BookResponse updateBook(String id, BookRequest request) {
        cacheManager.getCache("booksById").evict(id);
        cacheManager.getCache("BooksBySearch").evict(id);
        cacheManager.getCache("books").clear();
        return bookRepository.findById(id)
                .map(book -> {
                    // Guardar el nombre del autor original
                    String originalAuthorName = book.getAuthor();
                    Set<String> originalGenres = book.getCategories();

                    // Actualizar campos
                    book.setTitle(request.getTitle());
                    book.setSynopsis(request.getSynopsis());
                    book.setCategories(request.getCategories());
                    book.setAuthor(request.getAuthor());
                    book.setPublisher(request.getPublisher());
                    book.setPublicationDate(request.getPublicationDate());
                    book.setPageCount(request.getPageCount());
                    book.setLanguage(request.getLanguage());
                    book.setCoverImageUrl(request.getCoverImageUrl());
                    book.setUpdatedAt(LocalDateTime.now());

                    // Verificar si el ISBN ha cambiado y si ya existe
                    if (!book.getIsbn().equals(request.getIsbn()) &&
                            bookRepository.existsByIsbn(request.getIsbn())) {
                        throw new BookAlreadyExistsException("Ya existe un libro con el ISBN: " + request.getIsbn());
                    }
                    book.setIsbn(request.getIsbn());

                    // Guardar el libro actualizado
                    Book updatedBook = bookRepository.save(book);
                    log.info("Libro actualizado con ID: {}", id);

                    // Actualizar la colección de autores si el autor ha cambiado
                    updateAuthorOnBookUpdate(originalAuthorName, updatedBook);

                    // Actualizar la colección de géneros si los géneros han cambiado
                    updateGenresOnBookUpdate(originalGenres, updatedBook);

                    return mapToBookResponse(updatedBook);
                })
                .orElseThrow(() -> new BookNotFoundException("No se puede actualizar. Libro no encontrado con ID: " + id));
    }

    private void updateAuthorWithNewBook(Book book) {
        Author author = authorRepository.findByName(book.getAuthor())
                .orElseGet(() -> Author.builder().name(book.getAuthor()).build());

        BookSummary summary = createBookSummary(book);

        // Evitar duplicados si el libro ya está en la lista
        author.getBooks().removeIf(bookSummary -> bookSummary.getBookId().equals(book.getId()));
        author.getBooks().add(summary);

        authorRepository.save(author);
        log.info("Autor '{}' actualizado con el libro '{}'", author.getName(), book.getTitle());
    }

    private void updateAuthorOnBookUpdate(String originalAuthorName, Book updatedBook) {
        String newAuthorName = updatedBook.getAuthor();

        // Si el nombre del autor no ha cambiado, no se hace nada
        if (originalAuthorName.equals(newAuthorName)) {
            return;
        }

        // Eliminar el libro del autor original
        authorRepository.findByName(originalAuthorName).ifPresent(author -> {
            author.getBooks().removeIf(summary -> summary.getBookId().equals(updatedBook.getId()));
            authorRepository.save(author);
            log.info("Libro '{}' eliminado del autor '{}'", updatedBook.getTitle(), originalAuthorName);
        });

        // Agregar el libro al nuevo autor
        updateAuthorWithNewBook(updatedBook);
    }

    private BookResponse mapToBookResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .synopsis(book.getSynopsis())
                .categories(book.getCategories())
                .isbn(book.getIsbn())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .publicationDate(book.getPublicationDate())
                .pageCount(book.getPageCount())
                .language(book.getLanguage())
                .coverImageUrl(book.getCoverImageUrl())
                .averageRating(book.getAverageRating())
                .ratingsCount(book.getRatingsCount())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .available(book.getAvailable())
                .loans(book.getLoans() != null ? book.getLoans().stream().map(loan -> LoanSummaryResponse.builder()
                        .id(loan.getId())
                        .loanDate(loan.getLoanDate())
                        .expectedReturnDate(loan.getExpectedReturnDate())
                        .returnDate(loan.getReturnDate())
                        .status(loan.getStatus())
                        .user(loan.getUser() != null ? LoanSummaryResponse.UserInfoResponse.builder()
                                .id(loan.getUser().getId())
                                .fullName(loan.getUser().getFullName())
                                .cardNum(loan.getUser().getCardNum())
                                .build() : null)
                        .build()).toList() : null)
                .build();
    }

    private void updateGenresWithNewBook(Book book) {
        BookSummary summary = createBookSummary(book);
        book.getCategories().forEach(genreName -> {
            Categories genre = genreRepository.findByName(genreName)
                    .orElseGet(() -> Categories.builder().name(genreName).build());

            genre.getBooks().removeIf(bookSummary -> bookSummary.getBookId().equals(book.getId()));
            genre.getBooks().add(summary);
            genreRepository.save(genre);
            log.info("Género '{}' actualizado con el libro '{}'", genreName, book.getTitle());
        });
    }

    private void updateGenresOnBookUpdate(Set<String> originalGenres, Book updatedBook) {
        Set<String> newGenres = updatedBook.getCategories();

        Set<String> originalGenreSet = originalGenres != null ? originalGenres : Collections.emptySet();
        Set<String> newGenreSet = newGenres != null ? newGenres : Collections.emptySet();

        // Géneros eliminados: están en el original pero no en el nuevo
        originalGenreSet.stream()
                .filter(genreName -> !newGenreSet.contains(genreName))
                .forEach(genreName ->
                        genreRepository.findByName(genreName).ifPresent(genre -> {
                            genre.getBooks().removeIf(summary -> summary.getBookId().equals(updatedBook.getId()));
                            genreRepository.save(genre);
                            log.info("Libro '{}' eliminado del género '{}'", updatedBook.getTitle(), genreName);
                        })
                );

        // Géneros añadidos: están en el nuevo pero no en el original
        newGenreSet.stream()
                .filter(genreName -> !originalGenreSet.contains(genreName))
                .forEach(genreName -> updateGenreWithBook(genreName, updatedBook));
    }

    private void updateGenreWithBook(String genreName, Book book) {
        Categories genre = genreRepository.findByName(genreName)
                .orElseGet(() -> Categories.builder().name(genreName).build());

        BookSummary summary = createBookSummary(book);
        genre.getBooks().removeIf(s -> s.getBookId().equals(book.getId())); // Evitar duplicados
        genre.getBooks().add(summary);
        genreRepository.save(genre);
    }
    private BookSummary createBookSummary(Book book) {
        return BookSummary.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .coverImageUrl(book.getCoverImageUrl())
                .build();
    }

    @Transactional
    public void updateBookAverageRating(@NotNull Book book) {



        // Obtener todas las reseñas del libro desde la colección Review
        List<Review> reviews = reviewRepository.findByBook_BookId(book.getId());

        if (reviews == null || reviews.isEmpty()) {
            book.setAverageRating(0.0);
            book.setRatingsCount(0);
        } else {
            double sum = reviews.stream()
                    .mapToInt(com.example.demo.controllers.domain.entity.Review::getRating)
                    .sum();
            double average = sum / reviews.size();
            book.setAverageRating(average);
            book.setRatingsCount(reviews.size());
        }

        book.setUpdatedAt(LocalDateTime.now());

        bookRepository.save(book);
        log.info("Promedio de reseñas actualizado para el libro con ID: {}. Promedio: {}, Total: {}",
                book.getId(), book.getAverageRating(), book.getRatingsCount());

        updateBookSummaryInAuthorAndGenre(book);
    }
    private void updateBookSummaryInAuthorAndGenre(Book book) {
        // Actualizar Author
        authorRepository.findByName(book.getAuthor()).ifPresent(author -> {
            author.getBooks().stream()
                    .filter(summary -> summary.getBookId().equals(book.getId()))
                    .forEach(summary -> summary.setAverageRating(book.getAverageRating())
                    );
            authorRepository.save(author);
        });

        // Actualizar Genre
        book.getCategories().forEach(genreName ->
                genreRepository.findByName(genreName).ifPresent(genre -> {
                    genre.getBooks().stream()
                            .filter(summary -> summary.getBookId().equals(book.getId()))
                            .forEach(summary -> summary.setAverageRating(book.getAverageRating()));
                    genreRepository.save(genre);
                })
        );
    }

    // Usuarios que tienen el libro como favoritos
    public List<UserSummary> getUsersWhoFavoritedBook(String bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Libro no encontrado con ID: " + bookId));
        return book.getFavoredByUsers();
    }

    public Book deleteReviewFromBook(Book book, Review review){
        if(book.getId().equals(review.getBook().getBookId())){
            book.setUpdatedAt(LocalDateTime.now());
            book.getReviews().removeIf(r -> r.getId().equals(review.getId()));
            return bookRepository.save(book);
        } else {
            throw new BookNotFoundException("El libro con ID: " + book.getId() + " no contiene la reseña con ID: " + review.getId());
        }
    }

}
