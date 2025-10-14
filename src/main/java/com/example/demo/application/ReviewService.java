package com.example.demo.application;

import com.example.demo.controllers.domain.Model.BookSummary;
import com.example.demo.controllers.domain.Model.UserSummary;
import com.example.demo.controllers.dto.ReviewRequest;
import com.example.demo.controllers.domain.entity.Review;
import com.example.demo.controllers.domain.entity.Users;
import com.example.demo.controllers.domain.entity.Book; // Importar la entidad Book
import com.example.demo.controllers.domain.repository.ReviewRepository;
import com.example.demo.controllers.domain.repository.BookRepository; // Importar BookRepository
import com.example.demo.controllers.domain.repository.UserRepository; // Importar UserRepository
import com.example.demo.controllers.dto.ReviewUpdateRequest;
import com.example.demo.controllers.exception.ResourceNotFoundException;
import com.example.demo.controllers.exception.BookNotFoundException; // Importar si no existe
import com.example.demo.controllers.exception.UserNotFoundException; // Importar si no existe
import com.example.demo.controllers.response.ReviewResponse;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookService bookService; // Para actualizar el promedio del libro
    
    @Autowired
    private final BookRepository bookRepository; // Inyectar BookRepository

    @Autowired
    private final UserRepository userRepository; // Inyectar UserRepository

    private final UserService userService;


    @Transactional
    public ReviewResponse createReview(ReviewRequest reviewRequest) {

        // Validar que el libro exista antes de crear la reseña
        var book = bookRepository.findById(reviewRequest.getBookId())
                .orElseThrow(() -> new BookNotFoundException("El libro no fue encontrado")); // Lanza BookNotFoundException si no existe

        var user = userRepository.findById(reviewRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + reviewRequest.getUserId()));

        Review review = Review.builder()
                .rating(reviewRequest.getRating())
                .comment(reviewRequest.getComment())
                .book(createBookSummary(book))
                .user(createUserSummary(user))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);

        // Actualizar la calificación promedio del libro
        bookService.updateBookAverageRating(book);

        // --- ACTUALIZAR LISTAS EMBEBIDAS ---
        updateBookWithReview(book, savedReview); // true para añadir
        updateUserWithReview(user, savedReview);

        return mapToDTO(savedReview);
    }

    private static UserSummary createUserSummary(Users user){
        return UserSummary.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .cardNum(user.getCardNum())
                .build();
    }

    private static BookSummary createBookSummary(Book book) {
        return BookSummary.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .coverImageUrl(book.getCoverImageUrl())
                .averageRating(book.getAverageRating())
                .build();
    }

    // --- MÉTODO AUXILIAR IMPLEMENTADO ---
    private void updateUserWithReview(Users user, Review review) {

        Users.BookInfo bookInfo = Users.BookInfo.builder()
                .id(review.getBook().getBookId())
                .title(review.getBook().getTitle())
                .coverImageUrl(review.getBook().getCoverImageUrl())
                .build();

        Users.Review userReview = Users.Review.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .reviewDate(review.getCreatedAt()) // Usar createdAt de Review como reviewDate
                .book(bookInfo) // Añadir book (denormalizado)
                .build();

        // remuevo el antiguo review
        user.getReviews().removeIf(r -> review.getId() != null && review.getId().equals(r.getId()));

        user.getReviews().add(userReview);

        userRepository.save(user);
    }

    // --- MÉTODO AUXILIAR IMPLEMENTADO ---
    private void updateBookWithReview(Book book, Review review) {


        Book.UserInfo userinfo = Book.UserInfo.builder()
                .id(review.getUser().getUserId())
                .fullName(review.getUser().getFullName())
                .cardNum(review.getUser().getCardNum())
                .build();

        Book.Review bookReview = Book.Review.builder()
                .id(review.getId())
                .user(userinfo)
                .rating(review.getRating())
                .comment(review.getComment())
                .reviewDate(review.getCreatedAt()) // Usar createdAt de Review como reviewDate
                // No incluimos bookId, bookTitle, etc. en Book.Review porque ya está en el Book
                .build();
        // busco el review en la lista y si existe lo remuevo
        book.getReviews().removeIf(r -> review.getId() != null && review.getId().equals(r.getId()));
        // luego lo agrego (esto funciona tanto para agregar como para actualizar)
        book.getReviews().add(bookReview);

        bookRepository.save(book);
    }

    public List<ReviewResponse> getReviewsByBookId(String bookId) {
        List<Review> reviews = reviewRepository.findByBook_BookId(bookId);
        return reviews.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // --- Este método ya está implementado correctamente ---
    public List<ReviewResponse> getReviewsByUserId(String userId) {
        List<Review> reviews = reviewRepository.findByUser_UserId(userId);
        return reviews.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public Double calculateAverageRatingByBookId(String bookId) {
        return reviewRepository.calculateAverageRatingByBookId(bookId);
    }

    @Transactional
    public ReviewResponse updateReview(ReviewUpdateRequest reviewRequest) {
        String id = reviewRequest.getId();
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con ID: " + id));

        String originalBookId = existingReview.getBook().getBookId();

        // Actualizar campos
        if (reviewRequest.getRating() != null) {
            existingReview.setRating(reviewRequest.getRating());
        }
        if (reviewRequest.getComment() != null) {
            existingReview.setComment(reviewRequest.getComment());
        }


        existingReview.setUpdatedAt(LocalDateTime.now());

        Review updatedReview = reviewRepository.save(existingReview);

        Book book = bookRepository.findById(updatedReview.getBook().getBookId())
                .orElseThrow(() -> new BookNotFoundException("El libro no fue encontrado")); // Lanza BookNotFoundException si no existe

        Users user = userRepository.findById(updatedReview.getUser().getUserId())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + updatedReview.getUser().getUserId()));

        // Recalcular promedio del libro
        bookService.updateBookAverageRating(book);

        // --- ACTUALIZAR LISTAS EMBEBIDAS ---
        // Remover de listas antiguas
        updateBookWithReview(book, updatedReview); // false para remover
        updateUserWithReview(user, updatedReview); // false para remover

        return mapToDTO(updatedReview);
    }

    public void deleteReviewById(String id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con ID: " + id));

        String bookId = review.getBook().getBookId();
        String userId = review.getUser().getUserId();

        // se borra el Review
        reviewRepository.deleteById(id);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("El libro no fue encontrado")); // Lanza BookNotFoundException si no existe

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado para borrar el review"));

        // Recalcular promedio del libro
        bookService.updateBookAverageRating(book);

        // borro el review del libro
        bookService.deleteReviewFromBook(book, review);
        // borro el review del usuario
        userService.deleteReviewFromUser(user, review);

    }

    private ReviewResponse mapToDTO(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .bookId(review.getBook().getBookId())
                .bookTitle(review.getBook().getTitle())
                .userId(review.getUser().getUserId()) // Añadir userId al DTO
                .userName(review.getUser().getFullName()) // Añadir userName al DTO
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .helpfulCount(review.getHelpfulCount())
                .build();
    }
}
