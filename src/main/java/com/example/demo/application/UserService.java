package com.example.demo.application;

import com.example.demo.controllers.domain.entity.Book;
import com.example.demo.controllers.domain.entity.Users;
import com.example.demo.controllers.domain.repository.UserRepository;

import com.example.demo.controllers.domain.repository.BookRepository;

import com.example.demo.controllers.dto.UserRequest;
import com.example.demo.controllers.exception.UserAlreadyExistsException;
import com.example.demo.controllers.exception.FavoriteAlreadyExistsException;
import com.example.demo.controllers.exception.BookNotFoundException;
import com.example.demo.controllers.exception.UserNotFoundException;
import com.example.demo.controllers.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.controllers.dto.FavoriteRequest;
import com.example.demo.controllers.domain.Model.BookSummary;


import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final BookRepository bookRepository;

    @Transactional
    public UserResponse createUser(UserRequest request) {

        // Verificar si ya existe un usuario con el mismo número de tarjeta
        if (userRepository.existsByCardNum(request.getCardNum())) {
            throw new UserAlreadyExistsException("Ya existe un usuario con el número de tarjeta: " + request.getCardNum());
        }

        // Mapear UserRequest a la entidad Users
        Users user = Users.builder()
                .cardNum(request.getCardNum())
                .fullName(request.getFullName())
                .address(request.getAddress())
                .email(request.getEmail())
                .number(request.getNumber())
                .build();

        // Guardar el usuario
        Users savedUser = userRepository.save(user);
        log.info("Usuario creado con ID: {}", savedUser.getId());

        // Convertir a DTO y retornar
        return mapToUserResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(String id) {
        return userRepository.findById(id)
                .map(this::mapToUserResponse)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + id));
    }

    public List<UserResponse> getUserByFullName(String fullName) {
        return userRepository.findByFullNameContainingIgnoreCase(fullName).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(Users user) {
        return UserResponse.builder()
                .id(user.getId())
                .cardNum(user.getCardNum())
                .fullName(user.getFullName())
                .address(user.getAddress())
                .email(user.getEmail())
                .number(user.getNumber())
                .reviews(user.getReviews())
                .favorites(user.getFavorites())
                .build();
    }

    //Añadir libros favoritos del usuario
    @Transactional
    public void addFavorite(String userId, String bookId) {

        Users user = userRepository.findById(userId).orElseThrow();
        Book book = bookRepository.findById(bookId).orElseThrow();

        // Verificar si ya existe en favoritos
        boolean alreadyExists = user.getFavorites().stream()
                .anyMatch(fav -> fav.getBookId().equals(bookId));
        if (alreadyExists) throw new FavoriteAlreadyExistsException("El libro ya existe en favoritos");

        // Nueva instancia de BookSummary
        BookSummary bookSummary = BookSummary.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .coverImageUrl(book.getCoverImageUrl())
                .averageRating(book.getAverageRating())
                .build();

        user.getFavorites().add(bookSummary);
        userRepository.save(user);
    }

    //Remover favorito
    @Transactional
    public void removeFavorite(String userId, String bookId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        // Remover por bookId
        boolean removed = user.getFavorites().removeIf(fav -> fav.getBookId().equals(bookId));

        if (!removed) {
            throw new BookNotFoundException("El libro con ID " + bookId + " no está en favoritos del usuario");
        }
        userRepository.save(user);
        log.info("Libro {} removido de favoritos del usuario {}", bookId, userId);

    }

    //Obtener libros favoritos del usuario
    public List<BookSummary> getUserFavorites(String userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));
        return user.getFavorites();
    }

}
