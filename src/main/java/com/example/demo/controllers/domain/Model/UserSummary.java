package com.example.demo.controllers.domain.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Clase que representa un resumen de un usuario. Se utiliza para incrustar información
 * básica de un usuario en otras entidades como Book, aplicando desnormalización
 * para optimizar las consultas de lectura.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumen de un usuario para ser incrustado en otras entidades")
public class UserSummary implements Serializable {

    @Schema(description = "ID del usuario", example = "60c72b2f9b1d8c001f8e4a3c")
    private String userId;

    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez")
    private String fullName;

    @Schema(description = "Email del usuario", example = "juan@example.com")
    private String email;

    @Schema(description = "Número de tarjeta del usuario", example = "CARD12345")
    private String cardNum;
}