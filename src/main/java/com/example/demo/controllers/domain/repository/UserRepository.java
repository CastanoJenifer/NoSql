package com.example.demo.controllers.domain.repository;

import com.example.demo.controllers.domain.entity.Author;
import com.example.demo.controllers.domain.entity.Users;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<Users, String> {

    Optional<Users> findByName(String full_name);
}

