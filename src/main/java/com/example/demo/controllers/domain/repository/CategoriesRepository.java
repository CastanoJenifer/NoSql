package com.example.demo.controllers.domain.repository;

import com.example.demo.controllers.domain.entity.Categories;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CategoriesRepository extends MongoRepository<Categories, String> {

    Optional<Categories> findByName(String name);
}
