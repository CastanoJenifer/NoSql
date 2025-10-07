package com.example.demo.controllers.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.util.Set;

/**
 * DTO para la creación y actualización de libros
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para crear o actualizar un libro")
public class BookRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 1, max = 200, message = "El título debe tener entre 1 y 200 caracteres")
    @Schema(
            description = "Título del libro",
            example = "Cien años de soledad",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String title;


    @NotBlank(message = "La sinopsis es obligatoria")
    @Size(min = 10, max = 2000, message = "La sinopsis debe tener entre 10 y 2000 caracteres")
    @Schema(
            description = "Resumen detallado del libro",
            example = "Una obra maestra del realismo mágico que narra la historia de la familia Buendía a lo largo de siete generaciones en el mítico pueblo de Macondo.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String synopsis;

    @NotEmpty(message = "Debe especificar al menos una categoría")
    @Schema(
            description = "Categorías del libro",
            example = "[\"Realismo magico\", \"Literatura latinoamericana\"]",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Set<@NotBlank(message = "La categoría no puede estar vacía") String> categories;


    @NotBlank(message = "El ISBN es obligatorio")
    @Pattern(regexp = "^(?=(?:\\D*\\d){10}(?:(?:\\D*\\d){3})?$)[\\d-]+",
            message = "El ISBN debe tener 10 o 13 dígitos numéricos (guiones opcionales)")
    @Schema(
            description = "Código ISBN del libro (10 o 13 dígitos)",
            example = "978-0307474728",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String isbn;

    @NotBlank(message = "El autor es obligatorio")
    @Schema(
            description = "Nombre completo del autor",
            example = "Gabriel García Márquez",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String author;

    @NotBlank(message = "La editorial es obligatoria")
    @Schema(
            description = "Editorial que publica el libro",
            example = "Editorial Sudamericana",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String publisher;

    @NotNull(message = "La fecha de publicación es obligatoria")
    @PastOrPresent(message = "La fecha de publicación no puede ser futura")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(
            description = "Fecha de publicación del libro",
            example = "1967-05-30",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate publicationDate;

    @Min(value = 1, message = "El número de páginas debe ser al menos 1")
    @Schema(
            description = "Número total de páginas",
            example = "471",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer pageCount;

    @NotBlank(message = "El idioma es obligatorio")
    @Size(min = 2, max = 10, message = "El idioma debe tener entre 2 y 10 caracteres")
    @Schema(
            description = "Idioma del libro (código ISO 639-1)",
            example = "es",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String language;

    @URL(message = "La URL de la portada no es válida")
    @Schema(
            description = "URL de la imagen de portada del libro",
            example = "https://m.media-amazon.com/images/I/71m+Qtq+HZL._AC_UF1000,1000_QL80_.jpg",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String coverImageUrl;

    @Builder.Default
    @Schema(
            description = "Calificación promedio del libro (0.0 a 5.0)",
            example = "4.5",
            defaultValue = "0.0"
    )
    private Double averageRating = 0.0;

    @Builder.Default
    @Schema(
            description = "Número de calificaciones recibidas",
            example = "1500",
            defaultValue = "0"
    )
    private Integer ratingsCount = 0;
}
