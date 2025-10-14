package com.example.demo.controllers.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta que representa una reseña de un libro")
public class ReviewResponse implements Serializable {

    @Schema(description = "ID único de la reseña", example = "60c72b2f4b5c3d1f2c9f1b3a")
    private String id;

    @Schema(description = "ID del libro reseñado", example = "60c72b2f4b5c3d1f2c9f1b2a")
    private String bookId;

    @Schema(description = "Título del libro reseñado", example = "Cien años de soledad")
    private String bookTitle;

    @Schema(description = "Autor del libro reseñado", example = "Gabriel García Márquez")
    private String bookAuthor;

    @Schema(description = "ID del usuario que realiza la reseña", example = "67042a1a8c5e5e0e4c0e9d3b") // Ajusta el ejempl
    private String userId;

    @Schema(description = "ID del usuario que realiza la reseña", example = "67042a1a8c5e5e0e4c0e9d3b") // Ajusta el ejempl
    private String userName;

    @Schema(description = "Calificación del libro (1 a 5 estrellas)", example = "4")
    private Integer rating;

    @Schema(description = "Comentario opcional sobre el libro")
    private String comment;

    @Schema(description = "Fecha y hora en que se creó la reseña")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha y hora de la última actualización de la reseña")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "Número total de votos útiles", example = "5")
    private Integer helpfulCount;
}
