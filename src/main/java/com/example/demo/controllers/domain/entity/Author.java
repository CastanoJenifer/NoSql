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


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "escritores")
@Schema(description = "Entidad que representa a un autor y los libros que ha escrito")
public class Author {

    @Id
    private String id;

    @NotBlank(message = "El nombre del autor es obligatorio")
    @Indexed(unique = true)
    @Schema(description = "Nombre completo del autor", example = "Gabriel García Márquez")
    private String name;

    @Schema(description = "Breve biografía del autor")
    private String biography;

    @NotBlank(message = "La nacionalidad del autor es obligatoria")
    @Schema(description = "Nacionalidad del autor")
    private String nationality;

    @Builder.Default
    @Schema(description = "Lista de los libros escritos por el autor (datos redundantes para optimizar lecturas")
    private List<BookSummary> books = new ArrayList<>();

}
