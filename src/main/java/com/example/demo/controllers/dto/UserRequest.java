package com.example.demo.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Solicitud para actualizar los datos de un usuario")
public class UserRequest {

    @NotBlank(message = "El nombre del usuario es obligatorio")
    @Schema(description = "Nombre completo del usuario", example = "Mónica Andrea Cifuentes Salcedo")
    private String fullName;

    @NotBlank(message = "El número de la tarjeta es obligatorio")
    @Indexed(unique = true)
    @Schema(description = "Número de tarjeta que identifica al usuario", example = "U-2025-001")
    private String cardNum;

    @NotBlank(message = "La dirección del usuario es obligatoria")
    @Schema(description = "Dirección del usuario", example = "Calle 5A #22b-61")
    private String address;

    @Email(message = "Debe ingresar un correo electrónico válido")
    @NotBlank(message = "El correo electrónico del usuario es obligatorio")
    @Schema(description = "Correo electrónico del usuario", example = "monica.cifuentes@correo.com")
    private String email;

    @NotBlank(message = "El número de teléfono es obligatorio")
    @Schema(description = "Número de teléfono del usuario", example = "3243685898")
    private String number;
}
