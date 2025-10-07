package com.example.demo.controllers.domain.entity;

import com.example.demo.controllers.domain.Model.BookSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "categorias")
@Schema(description = "Entidad que representa las categorias y su relación con los libros")
public class Categories {

    @Id
    private String id;

    @NotBlank(message = "La categoría es obligatoria")
    @Indexed(unique = true)
    @Schema(description = "Nombre del categoria")
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    @Schema(description = "Breve descripción de la categoría")
    private String description;

    @Builder.Default
    @Schema(description = "Lista de los libros que pertenecen a la categoría (datos redundantes para optimizar lecturas")
    private List<BookSummary> books = new ArrayList<>();
}
