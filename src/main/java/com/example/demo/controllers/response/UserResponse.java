package com.example.demo.controllers.response;

import com.example.demo.controllers.domain.Model.BookSummary;
import com.example.demo.controllers.domain.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de la API para un usuario, incluyendo una lista de sus libros favoritos, prestamos y reseñas")
public class UserResponse implements Serializable {

    @Id
    private String id;

    @Schema(description = "Número de tarjeta que identifica al usuario")
    private String cardNum;

    @Schema(description = "Nombre completo del usuario")
    private String fullName;

    @Schema(description = "Dirección del usuario")
    private String address;

    @Schema(description = "Correo electrónico del usuario")
    private String email;

    @Schema(description = "Número de teléfono del usuario")
    private String number;

    //@Schema(description = "Lista de los prestamos del usuario")
    //private List<BookLoans> loans;

    @Schema(description = "Lista de las reseñas hechas por el usuario")
    private List<Review> reviews;

    @Schema(description = "Lista de los libros favoritos del usuario")
    private List<BookSummary> favorites;
}
