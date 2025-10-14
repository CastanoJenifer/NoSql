package com.example.demo.controllers.domain.entity;

import com.example.demo.controllers.domain.Model.BookSummary;
import com.example.demo.controllers.domain.Model.UserSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "prestamos")
@Schema(description = "Entidad que representa un préstamo de un libro a un usuario")
public class Loan {
    @Id
    private String id;

    @NotBlank(message = "El estado del prestamo es obligatorio")
    @Schema(description = "Estado del prestamo", example = "Entregado, vencido, Prestado")
    private String status;

    @NotNull(message = "la fecha del prestamo es obligatoria")
    @Schema(description = "Fecha del prestamo", example = "2025-02-20")
    private LocalDate loanDate;

    @NotNull()
    @Schema(description = "Fecha de devolución esperada", example = "2025-03-04")
    private LocalDate expectedReturnDate;

    @Schema(description = "Fecha de la devolución", example = "2025-03-03")
    private LocalDate returnDate;

    @Schema(description = "Información resumida del libro que fue prestado")
    private BookSummary book;

    @Schema(description = "Información resumida del usuario que realizó el préstamo")
    private UserSummary user;
}