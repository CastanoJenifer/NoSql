package com.example.demo.controllers.domain.repository;

import com.example.demo.controllers.domain.entity.Author;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AuthorRepository extends MongoRepository<Author, String> {
    /**
     * Busca un autor por su nombre.
     * @param name Nombre del autor.
     * @return Un Optional que contiene al autor si se encuentra.
     */
    Optional<Author> findByName(String name);
}
