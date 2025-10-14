package com.example.demo.controllers.domain.entity;

import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.example.demo.controllers.domain.Model.UserSummary;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection= "libros")
@Schema(description = "Entidad que representa a un libro con los autores y generos relacionados a este")
public class Book {
    @Id
    private String id;

    @NotBlank(message = "El título es obligatorio")
    @Schema(description = "Título del libro")
    @Size(min = 1, max = 200, message = "El título debe tener entre 1 y 200 caracteres")
    private String title;

    @NotBlank(message = "La sinopsis es obligatoria")
    @Schema(description = "Breve descripción del libro")
    @Size(min = 10, max = 2000, message = "La sinopsis debe tener entre 10 y 2000 caracteres")
    private String synopsis;

    @Schema(description = "Nombre completo del autor")
    @NotBlank(message = "El autor es obligatorio")
    private String author;

    @Schema(description = "Categorías relacionadas")
    private Set<@NotBlank(message = "Debe especificar al menos una categoria") String> categories;

    @NotBlank(message = "El ISBN es obligatorio")
    @Pattern(regexp = "^(?=(?:\\D*\\d){10}(?:(?:\\D*\\d){3})?$)[\\d-]+",
            message = "El ISBN debe tener 10 o 13 dígitos numéricos (guiones opcionales)")
    @Indexed(unique = true)
    private String isbn;

    @NotBlank(message = "La editorial es obligatoria")
    private String publisher;

    @NotBlank(message = "La fecha de publicación es obligatoria")
    @PastOrPresent(message = "La fecha de publicación no puede ser futura")
    private LocalDate publicationDate;

    @Min(value = 1, message = "El número de páginas debe ser de al menos 1")
    private Integer pageCount;

    @NotBlank(message = "El idioma es obligatorio")
    private String language;

    @Schema(description = "Lista de usuarios que tienen este libro como favorito")
    private List<UserSummary> favoredByUsers = new ArrayList<>();


    private String coverImageUrl;
    private Double averageRating;
    private Integer ratingsCount;
    private List<Review> reviews = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;


    @Schema(description = "Lista de los prestamos del libro")
    private List<LoanSummary> loans = new ArrayList<>();

    @Schema(description = "Disponibilidad del libro para préstamo por defecto 'true'")
    private Boolean available = true;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Review {

        private String id;

        private UserInfo user;

        @Min(1) @Max(5)
        private Integer rating;
        private String comment;
        private LocalDateTime reviewDate;


    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String fullName;
        private String cardNum;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanSummary {

        private String id;

        private LocalDate loanDate;
        private LocalDate expectedReturnDate;
        private String status;
        private LocalDate returnDate; // Nueva fecha de devolución, puede ser null
        private UserInfo user;


    }

}
