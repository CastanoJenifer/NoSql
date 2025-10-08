package com.example.demo.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para crear o actualizar una reseña de un libro")
public class ReviewRequest {

    @Schema(description = "ID del libro a reseñar", example = "67042a1a8c5e5e0e4c0e9d3a", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El ID del libro es obligatorio")
    private String bookId;

    @Schema(description = "Calificación del libro (1 a 5 estrellas)", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer rating;

    @Schema(description = "Comentario opcional sobre el libro", example = "Excelente libro, muy recomendado")
    @Size(max = 1000, message = "El comentario no puede exceder los 1000 caracteres")
    private String comment;
}