package com.example.demo.controllers;

import com.example.demo.application.UserService;
import com.example.demo.controllers.dto.BookRequest;
import com.example.demo.controllers.dto.UserRequest;
import com.example.demo.controllers.response.BookResponse;
import com.example.demo.controllers.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.demo.controllers.dto.FavoriteRequest;
import com.example.demo.controllers.domain.Model.BookSummary;


import java.util.List;

@Validated
@RestController
@RequestMapping("/user")
@Tag(name = "Controlador para los usuarios")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Crear un nuevo usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe un usuario con el número de tarjeta")
    })
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse response = userService.createUser(userRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Obtener todos los usuarios")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un usuario por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    //Favoritos
    @PostMapping("/{userId}/favorites/{bookId}")
    @Operation(summary = "Agregar un libro a favoritos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libro agregado a favoritos exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario o libro no encontrado"),
            @ApiResponse(responseCode = "409", description = "El libro ya está en favoritos")
    })
    public ResponseEntity<String> addFavorite(
            @PathVariable String userId,
            @PathVariable String bookId) {

        userService.addFavorite(userId, bookId);
        return ResponseEntity.ok("Libro agregado a favoritos exitosamente");
    }

    @DeleteMapping("/{userId}/favorites/{bookId}")
    @Operation(summary = "Remover un libro de favoritos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libro removido de favoritos exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<String> removeFavorite(
            @PathVariable String userId,
            @PathVariable String bookId) {
        userService.removeFavorite(userId, bookId);
        return ResponseEntity.ok("Libro removido de favoritos exitosamente");
    }

    @GetMapping("/{userId}/favorites")
    @Operation(summary = "Obtener todos los libros favoritos de un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de favoritos obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<List<BookSummary>> getUserFavorites(@PathVariable String userId) {
        List<BookSummary> favorites = userService.getUserFavorites(userId);
        return ResponseEntity.ok(favorites);
    }
}
