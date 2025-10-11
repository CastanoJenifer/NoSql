package com.example.demo.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteRequest {

    @NotBlank
    @Schema(description = "ID del libro", example = "60c72b2f9b1d8c001f8e4a3c")
    private String bookId;

    @Schema(description = "Título del libro", example = "Cien años de soledad")
    private String title;

    @Schema(description = "URL de la portada", example = "https://ejemplo.com/portada.jpg")
    private String coverImageUrl;

    @Schema(description = "Calificación promedio", example = "4.5")
    private Double averageRating;
}