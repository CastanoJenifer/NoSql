package com.example.demo.controllers.domain.repository;

import com.example.demo.controllers.domain.entity.Review;
import com.example.demo.controllers.domain.entity.Review;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    // Buscar reseñas por ID del libro
    List<Review> findByBook_BookId(String bookId);

    List<Review> findByRating(int rating);

    // --- NUEVO: Buscar reseñas por ID del usuario ---
    List<Review> findByUser_UserId(String userId);

    // Calcular promedio de calificaciones por libro usando agregación
    @Aggregation(pipeline = {
            "{ $match: { bookId: ?0 } }",
            "{ $group: { _id: null, averageRating: { $avg: '$rating' } } }",
            "{ $project: { _id: 0, averageRating: '$averageRating' } }"
    })
    Double calculateAverageRatingByBookId(String bookId);

    // Opcional: Eliminar reseñas por ID del libro (útil si se elimina un libro)
    void deleteByBook_BookId(String bookId);
}
