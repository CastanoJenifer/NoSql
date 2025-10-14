package com.example.demo.controllers.domain.repository;

import com.example.demo.controllers.domain.entity.Book;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends MongoRepository <Book, String> {

    /**
     * Busca un libro por su ISBN
     * @param isbn ISBN del libro a buscar
     * @return Optional con el libro si se encuentra
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Verifica si existe un libro con el ISBN especificado
     * @param isbn ISBN a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByIsbn(String isbn);

    /**
     * Busca libros por título (búsqueda insensible a mayúsculas/minúsculas)
     * @param title Título o parte del título a buscar
     * @return Lista de libros que coinciden con el título
     */
    List<Book> findByTitleContainingIgnoreCase(String title);

    /**
     * Busca libros por autor (búsqueda insensible a mayúsculas/minúsculas)
     * @param author Nombre del autor o parte del mismo
     * @return Lista de libros del autor
     */
    List<Book> findByAuthorContainingIgnoreCase(String author);

    /**
     * Busca libros por género
     * @param genre Género a buscar
     * @return Lista de libros que pertenecen al género especificado
     */
    @Query("{ 'genres': { $in: [?0] } }")
    List<Book> findByGenre(String genre);

    /**
     * Busca libros publicados después de un año específico
     * @param year Año de publicación
     * @return Lista de libros publicados después del año especificado
     */
    @Query("{ 'publicationDate': { $gte: ?0 } }")
    List<Book> findByPublicationYearAfter(int year);

    /**
     * Busca libros con una calificación mínima
     * @param minRating Calificación mínima (0-5)
     * @return Lista de libros con calificación igual o superior a la especificada
     */
    @Query("{ 'averageRating': { $gte: ?0 } }")
    List<Book> findByRatingGreaterThanEqual(double minRating);

    /**
     * Busca libros por editorial
     * @param publisher Nombre de la editorial
     * @return Lista de libros publicados por la editorial
     */
    List<Book> findByPublisher(String publisher);

    /**
     * Cuenta la cantidad de libros por autor
     * @param author Nombre del autor
     * @return Cantidad de libros del autor
     */
    long countByAuthor(String author);

    /**
     * Elimina libros por ISBN
     * @param isbn ISBN del libro a eliminar
     * @return Cantidad de libros eliminados
     */
    long deleteByIsbn(String isbn);

    /**
     * Busca libros por disponibilidad
     * @param available true para disponibles, false para prestados
     * @return Lista de libros según disponibilidad
     */
    List<Book> findByAvailable(boolean available);

    /**
     * Encuentra libros por el id del usuario que hizo su prestamo
     * @param userId id del usuario a buscar
     * @return Lista de ibros encontrados
     */
    List<Book> findByLoans_User_Id(String userId);

    /**
     * Encuentra libros por el id del usuario que tiene en favoritos
     * @param userId id del usuario a buscar
     * @return Lista de ibros encontrados
     */
    List<Book> findByFavoredByUsers_UserId(String userId);

}
