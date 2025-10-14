package com.example.demo.controllers;

import com.example.demo.application.LoanService;
import com.example.demo.controllers.domain.entity.Loan;
import com.example.demo.controllers.dto.LoanRequest;
import com.example.demo.controllers.response.LoanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/loans")
@Tag(name = "Préstamos", description = "API para la gestión de préstamos de libros")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;


    @PostMapping
    @Operation(summary = "Crear un nuevo préstamo de libro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Préstamo creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Libro o usuario no encontrado")
    })
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody LoanRequest loanRequest) {
        LoanResponse response = loanService.createLoan(loanRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/return")
    @Operation(summary = "Marcar un préstamo como entregado (solo si está en estado 'Prestado' o 'Vencido')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Préstamo marcado como entregado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Cambio de estado no permitido"),
            @ApiResponse(responseCode = "404", description = "Préstamo no encontrado")
    })
    public ResponseEntity<LoanResponse> markAsReturned(@PathVariable String id) {
        LoanResponse response = loanService.markAsReturned(id);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    @Operation(summary = "Obtener todos los préstamos de libros")
    public List<Loan> getAllLoans() {
        return loanService.getAllLoans();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un préstamo por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Préstamo encontrado"),
            @ApiResponse(responseCode = "404", description = "Préstamo no encontrado")
    })
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable String id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un préstamo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Préstamo eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Préstamo no encontrado")
    })
    public void deleteLoan(@PathVariable String id) {
        loanService.deleteLoan(id);
    }

}
