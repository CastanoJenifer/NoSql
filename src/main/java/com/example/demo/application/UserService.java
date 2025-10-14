package com.example.demo.application;

import com.example.demo.controllers.domain.entity.Book;
import com.example.demo.controllers.domain.entity.Loan;
import com.example.demo.controllers.domain.entity.Review;
import com.example.demo.controllers.domain.entity.Users;
import com.example.demo.controllers.domain.repository.LoanRepository;
import com.example.demo.controllers.domain.repository.UserRepository;
import com.example.demo.controllers.domain.repository.BookRepository;

import com.example.demo.controllers.dto.UserRequest;
import com.example.demo.controllers.exception.*;
import com.example.demo.controllers.response.UserResponse;
import com.example.demo.controllers.response.LoanSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.controllers.domain.Model.BookSummary;
import com.example.demo.controllers.domain.Model.UserSummary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final BookRepository bookRepository;

    private final LoanRepository loanRepository;

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

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(String id) {
        return userRepository.findById(id)
                .map(this::mapToUserResponse)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUserByFullName(String fullName) {
        return userRepository.findByFullNameContainingIgnoreCase(fullName).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(String id, UserRequest request) {
        return userRepository.findById(id)
                .map(user -> {

                    String oldCardNum = user.getCardNum();

                    // Actualizar campos
                    user.setCardNum(request.getCardNum());
                    user.setFullName(request.getFullName());
                    user.setAddress(request.getAddress());
                    user.setEmail(request.getEmail());
                    user.setNumber(request.getNumber());

                    // Verificar si el cardNum ha cambiado y si ya existe
                    if (!oldCardNum.equals(request.getCardNum()) &&
                            userRepository.existsByCardNum(request.getCardNum())) {
                        throw new UserAlreadyExistsException("Ya existe un usuario con el número de tarjeta: " + request.getCardNum());
                    }
                    user.setCardNum(request.getCardNum());

                    // Guardar el usuario actualizado
                    Users updatedUser = userRepository.save(user);
                    log.info("User actualizado con ID: {}", id);

                    updateUserSummaryInLoans(updatedUser);
                    updateUserSummaryInBooks(updatedUser);

                    return mapToUserResponse(updatedUser);
                })
                .orElseThrow(() -> new UserNotFoundException("No se puede actualizar. Usuario no encontrado con ID: " + id));
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
                .loans(user.getLoans() != null ? user.getLoans().stream().map(loan -> LoanSummaryResponse.builder()
                        .id(loan.getId())
                        .loanDate(loan.getLoanDate())
                        .expectedReturnDate(loan.getExpectedReturnDate())
                        .returnDate(loan.getReturnDate())
                        .status(loan.getStatus())
                        .user(null)
                        .book(loan.getBook() != null ? LoanSummaryResponse.BookInfoResponse.builder()
                                .id(loan.getBook().getId())
                                .title(loan.getBook().getTitle())
                                .coverImageUrl(loan.getBook().getCoverImageUrl())
                                .build() : null)
                        .build()).toList() : null)
                .build();
    }

    private void updateUserSummaryInLoans(Users user) {

        UserSummary updatedSummary = UserSummary.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .cardNum(user.getCardNum())
                .build();

        List<Loan> loans = loanRepository.findByUser_UserId(user.getId());

        if (loans.isEmpty()){
            log.info("No se encontraron préstamos asociados al usuario con ID: {}", user.getId());
            return;
        }

        loans.forEach(loan -> {
            loan.setUser(updatedSummary);
        });

        loanRepository.saveAll(loans);

        log.info("Se actualizaron {} préstamos con la información del usuario {}", loans.size(), user.getFullName());
    }

    private void updateUserSummaryInBooks(Users user) {

        List<Book> loanBooks = bookRepository.findByLoans_User_Id(user.getId());

        for (Book book : loanBooks) {
            if (book.getLoans() != null) {
                book.getLoans().forEach(loan -> {
                    if (loan.getUser() != null && user.getId().equals(loan.getUser().getId())) {
                        loan.getUser().setFullName(user.getFullName());
                        loan.getUser().setCardNum(user.getCardNum());
                    }
                });
                bookRepository.save(book);
                log.info("Actualizado usuario {} en préstamo del libro '{}'", user.getFullName(), book.getTitle());
            }
        }

        List<Book> favoredBooks = bookRepository.findByFavoredByUsers_UserId(user.getId());

        for (Book book : favoredBooks) {
            if (book.getFavoredByUsers() != null) {
                book.getFavoredByUsers().forEach(favored -> {
                    if (user.getId().equals(favored.getUserId())) {
                        favored.setFullName(user.getFullName());
                        favored.setEmail(user.getEmail());
                        favored.setCardNum(user.getCardNum());
                    }
                });
                bookRepository.save(book);
                log.info("Actualizado usuario {} en favoritos del libro '{}'", user.getFullName(), book.getTitle());
            }
        }
    }

    @Transactional
    public void deleteUser(String id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No se puede eliminar. Usuario no encontrado con ID: " + id));

        // Eliminar el usuario de prestamos
        List<Loan> userLoans = loanRepository.findByUser_UserId(id);
        boolean hasActiveLoans = userLoans.stream()
                .anyMatch(loan -> "Prestado".equalsIgnoreCase(loan.getStatus()) ||
                        "vencido".equalsIgnoreCase(loan.getStatus()));

        if (hasActiveLoans) {
            throw new IllegalStateException("No se puede eliminar el usuario porque tiene préstamos activos o vencidos.");
        }

        if (!userLoans.isEmpty()) {
            loanRepository.deleteAll(userLoans);
            log.info("Préstamos entregados del usuario {} eliminados", user.getFullName());
        }

        // Eliminar el usuario de libros
        List<Book> favoredBooks = bookRepository.findByFavoredByUsers_UserId(id);
        for (Book book : favoredBooks) {
            if (book.getFavoredByUsers() != null) {
                book.getFavoredByUsers().removeIf(favored -> id.equals(favored.getUserId()));
                bookRepository.save(book);
                log.info("Usuario {} eliminado de favoritos del libro '{}'", user.getFullName(), book.getTitle());
            }
        }

        List<Book> booksWithLoans = bookRepository.findByLoans_User_Id(id);
        for (Book book : booksWithLoans) {
            if (book.getLoans() != null) {
                book.getLoans().forEach(loan -> {
                    if (loan.getUser() != null && id.equals(loan.getUser().getId())) {
                        loan.setUser(null);
                    }
                });
                bookRepository.save(book);
            }
        }
        userRepository.delete(user);
        log.info("Usuario eliminado con ID: {}", id);
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

        // Nueva instancia de UserSummary
        UserSummary userSummary = UserSummary.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .cardNum(user.getCardNum())
                .build();


        user.getFavorites().add(bookSummary);
        book.getFavoredByUsers().add(userSummary);

        userRepository.save(user);
        bookRepository.save(book);
    }

    //Remover favorito
    @Transactional
    public void removeFavorite(String userId, String bookId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        Book book = bookRepository.findById(bookId).orElseThrow();

        // Remover por bookId
        boolean removed = user.getFavorites().removeIf(fav -> fav.getBookId().equals(bookId));
        if (!removed) {
            throw new BookNotFoundException("El libro con ID " + bookId + " no está en favoritos del usuario");
        }

        book.getFavoredByUsers().removeIf(fav -> fav.getUserId().equals(userId));


        userRepository.save(user);
        bookRepository.save(book);
        log.info("Libro {} removido de favoritos del usuario {}", bookId, userId);

    }

    //Obtener libros favoritos del usuario
    @Transactional(readOnly = true)
    public List<BookSummary> getUserFavorites(String userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));
        return user.getFavorites();
    }

    public Users deleteReviewFromUser(Users user, Review review){
        if(user.getId().equals(review.getBook().getBookId())){
            user.getReviews().removeIf(r -> r.getId().equals(review.getId()));
            return userRepository.save(user);
        } else {
            throw new BookNotFoundException("El user con ID: " + user.getId() + " no contiene la reseña con ID: " + review.getId());
        }
    }

}
