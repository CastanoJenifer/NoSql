package com.example.demo.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Solicitud para actualizar los datos de un autor")
public class AuthorUpdateRequest {

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nuevo nombre completo del autor", example = "Gabriel J. García Márquez", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Biografía actualizada del autor", example = "Escritor, guionista y periodista colombiano, ganador del Premio Nobel de Literatura en 1982.")
    private String biography;

    @Schema(
            description = "Nacionalidad del autor",
            example = "Colombiano"
    )
    private String nationality;
}
