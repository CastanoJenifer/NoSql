package com.example.demo.controllers;



import com.example.demo.controllers.dto.ReviewRequest;
import com.example.demo.controllers.response.ReviewResponse;
import com.example.demo.application.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Operaciones relacionadas con las reseñas de libros")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Crear una nueva reseña")
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest reviewRequest) {
        ReviewResponse createdReview = reviewService.createReview(reviewRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    @GetMapping("/book/{bookId}")
    @Operation(summary = "Obtener reseñas por ID del libro")
    public ResponseEntity<List<ReviewResponse>> getReviewsByBookId(@PathVariable String bookId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByBookId(bookId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/book/{bookId}/average-rating")
    @Operation(summary = "Calcular y obtener la calificación promedio de un libro")
    public ResponseEntity<Double> getAverageRatingByBookId(@PathVariable String bookId) {
        Double averageRating = reviewService.calculateAverageRatingByBookId(bookId);
        return ResponseEntity.ok(averageRating);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una reseña existente")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable String id,
            @Valid @RequestBody ReviewRequest reviewRequest) {
        ReviewResponse updatedReview = reviewService.updateReview(id, reviewRequest);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una reseña por ID")
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        reviewService.deleteReviewById(id);
        return ResponseEntity.noContent().build();
    }
}