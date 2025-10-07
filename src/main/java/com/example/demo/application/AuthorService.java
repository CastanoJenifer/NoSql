package com.example.demo.application;

import com.example.demo.controllers.domain.entity.Author;
import com.example.demo.controllers.domain.repository.AuthorRepository;
import com.example.demo.controllers.domain.repository.BookRepository;
import com.example.demo.controllers.dto.AuthorUpdateRequest;
import com.example.demo.controllers.exception.AuthorNotFoundException;
import com.example.demo.controllers.response.AuthorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final CacheManager cacheManager;

    @Transactional(readOnly = true)
    @Cacheable(value="authors")
    public List<AuthorResponse> getAllAuthors() {
        return authorRepository.findAll().stream()
                .map(this::mapToAuthorResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value="authorById")
    public AuthorResponse getAuthorById(String id) {
        return authorRepository.findById(id)
                .map(this::mapToAuthorResponse)
                .orElseThrow(() -> new AuthorNotFoundException("Autor no encontrado con ID: " + id));
    }

    @Transactional
    public AuthorResponse updateAuthor(String id, AuthorUpdateRequest request) {

        cacheManager.getCache("authorById").evict(id);
        cacheManager.getCache("authors").clear();

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException("Autor no encontrado con ID: " + id));

        String oldName = author.getName();
        String newName = request.getName();

        author.setName(newName);
        author.setBiography(request.getBiography());

        Author updatedAuthor = authorRepository.save(author);

        // Si el nombre ha cambiado, actualizar los libros de forma asíncrona
        if (!oldName.equals(newName)) {
            updateBookAuthorNamesAsync(oldName, newName);
        }

        return mapToAuthorResponse(updatedAuthor);
    }

    /**
     * Actualiza el nombre del autor en todos sus libros de forma asíncrona.
     * Esto evita bloquear la respuesta de la API mientras se realiza una operación potencialmente larga.
     */
    @Async
    public CompletableFuture<Void> updateBookAuthorNamesAsync(String oldName, String newName) {
        log.info("Iniciando actualización asíncrona de libros del autor '{}' a '{}'", oldName, newName);
        return CompletableFuture.runAsync(() -> {
            bookRepository.findByAuthorContainingIgnoreCase(oldName).forEach(book -> {
                book.setAuthor(newName);
                bookRepository.save(book);
                log.debug("Actualizado el autor del libro con ID: {}", book.getId());
            });
            log.info("Finalizada la actualización de libros para el autor '{}'", newName);
        });
    }

    private AuthorResponse mapToAuthorResponse(Author author) {
        return AuthorResponse.builder()
                .id(author.getId())
                .name(author.getName())
                .biography(author.getBiography())
                .books(author.getBooks())
                .build();
    }
}
