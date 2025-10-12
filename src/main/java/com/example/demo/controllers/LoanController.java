package com.example.demo.controllers;

import com.example.demo.application.LoanService;
import com.example.demo.controllers.domain.entity.Loan;
import com.example.demo.controllers.dto.LoanRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/loans")
@Tag(name = "Controlador para la gestión de préstamos de libros")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;


    @PostMapping
    @Operation(summary = "Crear un nuevo préstamo de libro")
    public ResponseEntity<Loan> createLoan(@Valid @RequestBody LoanRequest loanRequest) {
        return ResponseEntity.ok(loanService.createLoan(loanRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un préstamo existente")
    public ResponseEntity<Loan> updateLoan(@PathVariable String id, @RequestBody Loan loan) {
        return loanService.updateLoan(id, loan)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Obtener todos los préstamos de libros")
    public List<Loan> getAllLoans() {
        return loanService.getAllLoans();
    }
}