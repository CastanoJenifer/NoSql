package com.example.demo.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Request para crear un prestamo de un libro a un usuario")
public class LoanRequest {

    // Loan Request es para crear prestamos, entonces status es obligatoriamente "Prestado"

    @Schema(description = "Fecha del prestamo (Si no se envía, Se asigna la fecha actual)", example = "2025-02-20")
    private LocalDate loanDate;

    @Schema(description = "Fecha de devolución esperada (No es obligatoria, se calculará 30 dias después de le fecha del prestamo)", example = "2025-03-04")
    private LocalDate expectedReturnDate;

    @NotBlank(message = "El ID del libro es obligatorio")
    @Schema(description = "Id del libro", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bookId;

    @NotBlank(message = "El ID del usuario es obligatorio")
    @Schema(description = "Id del usuario", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;
}