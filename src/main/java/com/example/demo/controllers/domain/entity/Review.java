package com.example.demo.controllers.domain.entity;


import com.example.demo.controllers.domain.Model.BookSummary;
import com.example.demo.controllers.domain.Model.UserSummary;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Reseñas")
public class Review {

    @Id
    private String id;

    // Datos del libro (denormalizados)
    @NotNull(message = "El libro es obligatorio")
    private BookSummary book;

    @NotNull(message = "El usuario es obligatorio")
    private UserSummary user;


    // Datos de la reseña
    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5)
    private Integer rating;

    @Size(max = 1000, message = "El comentario no puede exceder los 1000 caracteres")
    private String comment;

    // Metadatos
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @Builder.Default
    private Integer helpfulCount = 0;
}
