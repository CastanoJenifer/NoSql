package com.example.demo.controllers;

import com.example.demo.application.BookService;
import com.example.demo.application.CategoryService;
import com.example.demo.controllers.domain.entity.Categories;
import com.example.demo.controllers.domain.repository.CategoriesRepository;
import com.example.demo.controllers.dto.CategoryUpdateRequest;
import com.example.demo.controllers.exception.CategoryNotFoundException;
import com.example.demo.controllers.response.BookResponse;
import com.example.demo.controllers.response.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Tag(name = "Controlador para las categorías de los libros")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoriesRepository categoriesRepository;
    private final BookService bookService;

    @GetMapping("/{name}")
    @Operation(summary = "Buscar un género y los libros asociados por su nombre")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<CategoryResponse> getGenreByName(@PathVariable String name) {
        return categoriesRepository.findByName(name)
                .map(this::mapToGenreResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new CategoryNotFoundException("Género no encontrado: " + name));
    }

    @PutMapping("/{name}")
    @Operation(summary = "Actualizar la descripción de un género")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Descripción del género actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Género no encontrado")
    })
    public ResponseEntity<CategoryResponse> updateGenreDescription(
            @PathVariable String name,
            @Valid @RequestBody CategoryUpdateRequest request) {
        Categories updateCategory = categoryService.updateGenreDescription(name, request.getDescription());
        return ResponseEntity.ok(mapToGenreResponse(updateCategory));
    }

    /*@GetMapping("/{name}/books-by-query")
    @Operation(summary = "Buscar libros por género (consulta directa a libros)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libros encontrados para el género"),
            @ApiResponse(responseCode = "404", description = "Género no encontrado o sin libros asociados")
    })
    public ResponseEntity<List<BookResponse>> getBooksByGenreQuery(@PathVariable String name) {
        List<BookResponse> books = bookService.findBooksByGenre(name);
        if (books.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(books);
    }*/

    @GetMapping("/{name}/books-in-memory")
    @Operation(summary = "Buscar libros por género (filtrado en memoria)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libros encontrados para el género"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada o sin libros asociados")
    })
    public ResponseEntity<List<BookResponse>> getBooksByGenreInMemoryQuery(@PathVariable String name) {
        List<BookResponse> books = bookService.findBooksByGenreInMemory(name);
        if (books.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(books);
    }

    private CategoryResponse mapToGenreResponse(Categories category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .books(category.getBooks())
                .build();
    }
}
