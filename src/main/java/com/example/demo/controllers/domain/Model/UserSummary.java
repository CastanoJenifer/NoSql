package com.example.demo.controllers.domain.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Resumen de un usuario para ser incrustado en otras entidades")
public class UserSummary {

    @Schema(description = "ID del usuario", example = "60c72b2f9b1d8c001f8e4a3c")
    private String userId;

    @Schema(description = "Nombre completo del usuario", example = "Carlos Pérez")
    private String fullName;

    @Schema(description = "Numero de tarjeta del usuario", example = "U-2025-001")
    private String cardNum;

    @Schema(description = "Correo electrónico del usuario", example = "user@gmail.com")
    private String email;
}
