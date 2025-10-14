package com.example.demo.controllers.domain.repository;

import com.example.demo.controllers.domain.entity.Users;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<Users, String> {

    /**
     * Busca usuarios por nombre
     * @param fullName Nombre del usuario a buscar
     * @return Optional con el usuario si se encuentra
     */
    List<Users> findByFullNameContainingIgnoreCase(String fullName);

    /**
     * Verifica si existe un usuario con el número de tarjeta especificado
     * @param cardNum Número de tarjeta a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByCardNum(String cardNum);

    /**
     * Busca un usuario por su cardNum
     * @param cardNum Número de tarjeta del usuario a buscar
     * @return Optional con el usuario si se encuentra
     */
    Optional<Users> findByCardNum(String cardNum);

    /**
     * Elimina usuarios por card_num
     * @param cardNum Número de tarjeta del usuario a eliminar
     * @return Cantidad de usuarios eliminados
     */
    long deleteByCardNum(String cardNum);

}

