package com.example.demo.controllers.domain.repository;

import com.example.demo.controllers.domain.entity.Users;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<Users, String> {

    Optional<Users> findByFullName(String full_name);

    /**
     * Verifica si existe un usuario con el número de tarjeta especificado
     * @param card_num ISBN a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByCardNum(String card_num);
}

