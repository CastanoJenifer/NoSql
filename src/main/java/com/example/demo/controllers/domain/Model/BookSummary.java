package com.example.demo.controllers.domain.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Clase que representa un resumen de un libro. Se utiliza para incrustar información
 * básica de un libro en otras entidades como Autor o Género, aplicando desnormalización
 * para optimizar las consultas de lectura.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumen de un libro para ser incrustado en otras entidades")

public class BookSummary implements Serializable {

    @Schema(description = "ID del libro", example = "60c72b2f9b1d8c001f8e4a3c")
    private String bookId;

    @Schema(description = "Título del libro", example = "Cien años de soledad")
    private String title;

    @Schema(description = "URL de la portada del libro", example = "https://ejemplo.com/portada.jpg")
    private String coverImageUrl;
}
