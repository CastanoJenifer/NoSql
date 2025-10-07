package com.example.demo.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para actualizar los datos de una categoría")
public class CategoryUpdateRequest {

    @NotBlank(message = "El nombre no puede estar vacío")
    @Schema(
            description = "Nombre de la categoría",
            example = "Realismo magico"
    )
    private String name;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Schema(
            description = "Descripción de la categoría",
            example = "El realismo mágico es una categoría o movimiento literario y artístico que fusiona la realidad cotidiana con elementos fantásticos o irreales, presentándolos como algo normal y común dentro de la narrativa."
    )
    private String description;
}
