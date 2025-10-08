package com.example.demo.application;

import com.example.demo.controllers.dto.ReviewRequest;
import com.example.demo.controllers.domain.entity.Review;
import com.example.demo.controllers.domain.repository.ReviewRepository;
import com.example.demo.controllers.exception.ResourceNotFoundException;
import com.example.demo.controllers.response.ReviewResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookService bookService; // Para actualizar el promedio del libro

    public ReviewResponse createReview(ReviewRequest reviewRequest) {
        // Validar que los campos requeridos no sean nulos
        if (reviewRequest.getBookId() == null || reviewRequest.getBookId().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del libro es obligatorio");
        }
        if (reviewRequest.getRating() == null) {
            throw new IllegalArgumentException("La calificación es obligatoria");
        }

        // Validar que el libro exista antes de crear la reseña
        var book = bookService.getBookById(reviewRequest.getBookId()); // Lanza BookNotFoundException si no existe

        Review review = Review.builder()
                .bookId(reviewRequest.getBookId())
                .bookTitle(book.getTitle())
                .bookAuthor(book.getAuthor())
                .rating(reviewRequest.getRating())
                .comment(reviewRequest.getComment())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);

        // Actualizar la calificación promedio del libro
        bookService.updateBookAverageRating(savedReview.getBookId());

        return mapToDTO(savedReview);
    }

    public List<ReviewResponse> getReviewsByBookId(String bookId) {
        List<Review> reviews = reviewRepository.findByBookId(bookId);
        return reviews.stream().map(this::mapToDTO).collect(Collectors.toList());
    }


    public Double calculateAverageRatingByBookId(String bookId) {
        return reviewRepository.calculateAverageRatingByBookId(bookId);
    }

    public ReviewResponse updateReview(String id, ReviewRequest reviewRequest) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con ID: " + id));

        // Solo permitir editar rating y comment
        if (reviewRequest.getRating() != null) {
            existingReview.setRating(reviewRequest.getRating());
        }
        if (reviewRequest.getComment() != null) {
            existingReview.setComment(reviewRequest.getComment());
        }
        existingReview.setUpdatedAt(LocalDateTime.now());

        Review updatedReview = reviewRepository.save(existingReview);

        // Recalcular promedio del libro
        bookService.updateBookAverageRating(updatedReview.getBookId());

        return mapToDTO(updatedReview);
    }

    public void deleteReviewById(String id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con ID: " + id));

        String bookId = review.getBookId();
        reviewRepository.deleteById(id);

        // Recalcular promedio del libro
        bookService.updateBookAverageRating(bookId);
    }

    private ReviewResponse mapToDTO(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .bookId(review.getBookId())
                .bookTitle(review.getBookTitle())
                .bookAuthor(review.getBookAuthor())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .helpfulCount(review.getHelpfulCount())
                .build();
    }
}
