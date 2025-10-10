package com.example.demo.controllers.domain.entity;


import com.example.demo.controllers.domain.Model.BookSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection= "usuarios")
@Schema(description = "Entidad que representa a un usuario y sus libros favoritos")
public class Users {

    @Id
    private String id;

    @NotBlank(message = "El número de la tarjeta es obligatorio")
    @Indexed(unique = true)
    @Schema(description = "Número de tarjeta que identifica al usuario", example = "U-2025-001")
    private String card_num;

    @NotBlank(message = "El nombre del usuario es obligatorio")
    @Schema(description = "Nombre completo del usuario", example = "Mónica Andrea Cifuentes Salcedo")
    private String full_name;

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

    //@Schema(description = "Lista de los prestamos del usuario")
    //private List<BookLoans> loans = new ArrayList<>();

    @Schema(description = "Lista de las reseñas hechas por el usuario")
    private List<Review> reviews = new ArrayList<>();

    @Schema(description = "Lista de los libros favoritos del usuario")
    private List<BookSummary> favorites = new ArrayList<>();
}
