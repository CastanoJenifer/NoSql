package com.example.demo.application;

import com.example.demo.controllers.dto.ReviewRequest;
import com.example.demo.controllers.domain.entity.Review;
import com.example.demo.controllers.domain.entity.Users;
import com.example.demo.controllers.domain.entity.Book; // Importar la entidad Book
import com.example.demo.controllers.domain.repository.ReviewRepository;
import com.example.demo.controllers.domain.repository.BookRepository; // Importar BookRepository
import com.example.demo.controllers.domain.repository.UserRepository; // Importar UserRepository
import com.example.demo.controllers.exception.ResourceNotFoundException;
import com.example.demo.controllers.exception.BookNotFoundException; // Importar si no existe
import com.example.demo.controllers.exception.UserNotFoundException; // Importar si no existe
import com.example.demo.controllers.response.ReviewResponse;
import lombok.RequiredArgsConstructor;
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

    public ReviewResponse createReview(ReviewRequest reviewRequest) {
        // Validar que los campos requeridos no sean nulos
        if (reviewRequest.getBookId() == null || reviewRequest.getBookId().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del libro es obligatorio");
        }
        if (reviewRequest.getUserId() == null || reviewRequest.getUserId().trim().isEmpty()) { // Validar userId
            throw new IllegalArgumentException("El ID del usuario es obligatorio");
        }
        if (reviewRequest.getRating() == null) {
            throw new IllegalArgumentException("La calificación es obligatoria");
        }

        // Validar que el libro exista antes de crear la reseña
        var book = bookService.getBookById(reviewRequest.getBookId()); // Lanza BookNotFoundException si no existe

        var user = userRepository.findById(reviewRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + reviewRequest.getUserId()));

        Review review = Review.builder()
                .bookId(reviewRequest.getBookId())
                .bookTitle(book.getTitle())
                .bookAuthor(book.getAuthor())
                .userId(reviewRequest.getUserId()) // Asignar userId
                .userName(user.getFullName()) // Asignar userName
                .rating(reviewRequest.getRating())
                .comment(reviewRequest.getComment())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);

        // Actualizar la calificación promedio del libro
        bookService.updateBookAverageRating(savedReview.getBookId());

        // --- ACTUALIZAR LISTAS EMBEBIDAS ---
        updateBookWithReview(savedReview, true); // true para añadir
        updateUserWithReview(savedReview, true);

        return mapToDTO(savedReview);
    }

    // --- MÉTODO AUXILIAR IMPLEMENTADO ---
    private void updateUserWithReview(Review review, boolean add) {
        Users user = userRepository.findById(review.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + review.getUserId()));

        // Mapear Review (colección) -> Users.Review (Book.Review)
        Users.Review userReview = Users.Review.builder()
                .userId(review.getUserId())
                .userName(review.getUserName())
                .rating(review.getRating())
                .comment(review.getComment())
                .reviewDate(review.getCreatedAt()) // Usar createdAt de Review como reviewDate
                .bookId(review.getBookId()) // Añadir bookId (denormalizado)
                .bookTitle(review.getBookTitle()) // Añadir bookTitle (denormalizado)
                .build();

        if (add) {
            // Evitar duplicados. Usar Objects.equals para manejar posibles nulls
            user.getReviews().removeIf(r -> Objects.equals(r.getUserId(), review.getUserId()) && Objects.equals(r.getReviewDate(), review.getCreatedAt()));
            user.getReviews().add(userReview);
        } else {
            // Remover por userId y reviewDate (createdAt) para identificar exactamente la reseña. Usar Objects.equals.
            user.getReviews().removeIf(r -> Objects.equals(r.getUserId(), review.getUserId()) && Objects.equals(r.getReviewDate(), review.getCreatedAt()));
        }
        userRepository.save(user);
    }

    // --- MÉTODO AUXILIAR IMPLEMENTADO ---
    private void updateBookWithReview(Review review, boolean add) {
        Book book = bookRepository.findById(review.getBookId())
                .orElseThrow(() -> new BookNotFoundException("Libro no encontrado con ID: " + review.getBookId()));

        Book.Review bookReview = Book.Review.builder()
                .userId(review.getUserId())
                .userName(review.getUserName())
                .rating(review.getRating())
                .comment(review.getComment())
                .reviewDate(review.getCreatedAt()) // Usar createdAt de Review como reviewDate
                // No incluimos bookId, bookTitle, etc. en Book.Review porque ya está en el Book
                .build();

        if (add) {
            // Evitar duplicados. Buscar por userId es más confiable si un usuario solo puede reseñar un libro una vez
            book.getReviews().removeIf(r -> r.getUserId().equals(review.getUserId()));
            book.getReviews().add(bookReview);
        } else {
            // Remover por userId y fecha para identificar exactamente la reseña
            book.getReviews().removeIf(r -> r.getUserId().equals(review.getUserId()) && r.getReviewDate().equals(review.getCreatedAt()));
        }
        bookRepository.save(book);
    }

    public List<ReviewResponse> getReviewsByBookId(String bookId) {
        List<Review> reviews = reviewRepository.findByBookId(bookId);
        return reviews.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // --- Este método ya está implementado correctamente ---
    public List<ReviewResponse> getReviewsByUserId(String userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return reviews.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public Double calculateAverageRatingByBookId(String bookId) {
        return reviewRepository.calculateAverageRatingByBookId(bookId);
    }

    public ReviewResponse updateReview(String id, ReviewRequest reviewRequest) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con ID: " + id));

        String originalUserId = existingReview.getUserId();
        String originalBookId = existingReview.getBookId();

        // Actualizar campos
        if (reviewRequest.getRating() != null) {
            existingReview.setRating(reviewRequest.getRating());
        }
        if (reviewRequest.getComment() != null) {
            existingReview.setComment(reviewRequest.getComment());
        }
        // Si userId o bookId pueden cambiar, actualizarlos también
        if (reviewRequest.getUserId() != null && !reviewRequest.getUserId().equals(existingReview.getUserId())) {
            // Validar nuevo usuario
            var user = userRepository.findById(reviewRequest.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + reviewRequest.getUserId()));
            existingReview.setUserId(reviewRequest.getUserId());
            existingReview.setUserName(user.getFullName());
        }
        if (reviewRequest.getBookId() != null && !reviewRequest.getBookId().equals(existingReview.getBookId())) {
            // Validar nuevo libro
            var book = bookService.getBookById(reviewRequest.getBookId()); // Lanza BookNotFoundException si no existe
            existingReview.setBookId(reviewRequest.getBookId());
            existingReview.setBookTitle(book.getTitle());
            existingReview.setBookAuthor(book.getAuthor());
        }
        existingReview.setUpdatedAt(LocalDateTime.now());

        Review updatedReview = reviewRepository.save(existingReview);

        // Recalcular promedio del libro
        bookService.updateBookAverageRating(updatedReview.getBookId());
        if (!originalBookId.equals(updatedReview.getBookId())) {
            bookService.updateBookAverageRating(updatedReview.getBookId());
        }

        // --- ACTUALIZAR LISTAS EMBEBIDAS ---
        // Remover de listas antiguas
        updateBookWithReview(existingReview, false); // false para remover
        updateUserWithReview(existingReview, false); // false para remover
        // Añadir a listas nuevas (o actualizar en la misma si no cambió)
        updateBookWithReview(updatedReview, true); // true para añadir
        updateUserWithReview(updatedReview, true); // true para añadir

        return mapToDTO(updatedReview);
    }

    public void deleteReviewById(String id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con ID: " + id));

        String bookId = review.getBookId();
        String userId = review.getUserId();

        reviewRepository.deleteById(id);

        // Recalcular promedio del libro
        bookService.updateBookAverageRating(bookId);

        // --- ACTUALIZAR LISTAS EMBEBIDAS ---
        updateBookWithReview(review, false);
        updateUserWithReview(review, false);
    }

    private ReviewResponse mapToDTO(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .bookId(review.getBookId())
                .bookTitle(review.getBookTitle())
                .bookAuthor(review.getBookAuthor())
                .userId(review.getUserId()) // Añadir userId al DTO
                .userName(review.getUserName()) // Añadir userName al DTO
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .helpfulCount(review.getHelpfulCount())
                .build();
    }
}
