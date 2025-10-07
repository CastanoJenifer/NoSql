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
@Schema(description = "Respuesta de la API para una categoría, incluyendo una lista de sus libros")
public class CategoryResponse implements Serializable {

    @Schema(description = "Id de la categoría")
    private String id;

    @Schema(description = "Nombre de la categoría")
    private String name;

    @Schema(description = "Descripción de la categoría")
    private String description;

    @Schema(description = "Libros de la categoría")
    private List<BookSummary> books;
}
