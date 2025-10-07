package com.example.demo.controllers.response;

import com.example.demo.controllers.domain.Model.BookSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de la API para un autor, incluyendo una lista de sus libros")
public class AuthorResponse implements Serializable {

    @Schema(description = "ID único del autor", example = "60c72b2f9b1d8c001f8e4a3d")
    private String id;

    @Schema(description = "Nombre completo del autor", example = "Gabriel García Márquez")
    private String name;

    @Schema(description = "Breve biografía del autor")
    private String biography;

    @Schema(description = "Nacionalidad del autor")
    private String nationality;

    @Schema(description = "Lista de resúmenes de libros escritos por el autor")
    private List<BookSummary> books;

}
